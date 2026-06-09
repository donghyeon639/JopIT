package com.main.jobit.domain.resume;

import com.main.jobit.ai.port.LlmPort;
import com.main.jobit.domain.resume.dto.ResumeFeedbackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 이력서 첨삭의 핵심 유스케이스. 두 입력 경로(텍스트 / 파일)를 받아
//   (필요 시) Tika 텍스트 추출 → 길이 검증 → 첨삭 프롬프트 조립 → LlmPort.generate() 호출
// 의 흐름으로 동일하게 수렴시킨다. LLM 구현체(현재 Claude CLI)에는 LlmPort 추상화만 의존하므로
// AI 백엔드를 교체해도 이 서비스는 영향을 받지 않는다.
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeFeedbackService {

    // LLM에 보내기 전 입력 길이의 하한/상한. 너무 짧으면 첨삭할 내용이 없고,
    // 너무 길면 토큰 비용·지연이 과도해지므로 사전 차단한다.
    // (참고: 파일 추출 단계의 MAX_CHARS 20만 자보다 작다 — 추출은 넉넉히 받되 LLM 입력은 더 보수적으로 제한.)
    private static final int MIN_CHARS = 50;
    private static final int MAX_CHARS = 50_000;

    private final LlmPort llmPort;          // AI 포트(어댑터 교체 지점) — 도메인은 이 인터페이스만 의존
    private final ResumeTextExtractor extractor;  // 파일 → 평문 텍스트 추출(Apache Tika 래퍼)

    // 텍스트 붙여넣기 경로: 추출 단계 없이 곧바로 정제 → 검증 → LLM.
    public ResumeFeedbackResponse fromText(String text, String jobCategory) {
        String cleaned = text == null ? "" : text.trim();  // null 안전 + 앞뒤 공백 제거
        validateLength(cleaned);
        String feedback = llmPort.generate(buildPrompt(cleaned, jobCategory));
        // 파일이 아니므로 감지된 타입은 항상 text/plain으로 고정.
        return new ResumeFeedbackResponse(feedback, cleaned.length(), "text/plain");
    }

    // 파일 업로드 경로: 먼저 Tika로 평문과 MIME 타입을 뽑고, 이후는 텍스트 경로와 동일한 검증·LLM 흐름.
    public ResumeFeedbackResponse fromFile(MultipartFile file, String jobCategory) {
        ResumeTextExtractor.Extracted extracted = extractor.extract(file);
        String text = extracted.text();
        validateLength(text);
        String feedback = llmPort.generate(buildPrompt(text, jobCategory));
        // 응답에 Tika가 감지한 실제 MIME 타입을 실어 클라이언트가 어떤 형식으로 인식됐는지 알 수 있게 한다.
        return new ResumeFeedbackResponse(feedback, text.length(), extracted.mimeType());
    }

    // LLM 호출 전 입력 길이 게이트. 하한/상한을 벗어나면 LLM을 부르기 전에 즉시 막아 비용·시간을 아낀다.
    private void validateLength(String text) {
        if (text.length() < MIN_CHARS) {
            throw new IllegalArgumentException(
                    "이력서 내용이 너무 짧습니다. 최소 " + MIN_CHARS + "자 이상 입력해주세요.");
        }
        if (text.length() > MAX_CHARS) {
            throw new IllegalArgumentException(
                    "이력서 내용이 너무 깁니다. 최대 " + MAX_CHARS + "자까지 지원합니다.");
        }
    }

    // 첨삭 프롬프트 조립. LLM에 "시니어 첨삭 코치" 역할과 고정된 출력 섹션 구조(1~5번)를 강제해
    // 응답 포맷을 일관되게 만든다. jobCategory가 있으면 직군 맥락을 주입해 직무 적합성 평가를 유도한다.
    private String buildPrompt(String resumeText, String jobCategory) {
        StringBuilder sb = new StringBuilder();
        // 시스템 역할 지시: 평가 관점(시니어 첨삭 코치)과 목적(부족한 점 구체 지적)을 먼저 못박는다.
        sb.append("당신은 IT 채용 시장에 정통한 시니어 이력서 첨삭 코치입니다. ");
        sb.append("아래 이력서를 검토하고, 어떤 부분이 부족한지 구체적으로 짚어주세요.\n\n");

        // 목표 직군이 주어졌을 때만 맥락을 추가 — 직무 관련성 평가의 근거가 된다.
        if (jobCategory != null && !jobCategory.isBlank()) {
            sb.append("## 지원자가 목표하는 직군\n").append(jobCategory).append("\n\n");
        }

        // 이력서 원문을 코드 펜스(```)로 감싸 본문과 지시문이 섞이지 않도록 명확히 구분한다.
        sb.append("## 이력서 원문\n");
        sb.append("```\n").append(resumeText).append("\n```\n\n");

        // 출력 스키마 강제: 섹션 순서·형식을 고정해 응답을 프런트에서 일관되게 렌더링할 수 있게 한다.
        sb.append("## 평가 기준 (반드시 아래 섹션 순서대로 마크다운으로 작성)\n");
        sb.append("### 1. 한 줄 총평\n");
        sb.append("이력서 전체 인상을 한두 문장으로 요약.\n\n");
        sb.append("### 2. 강점\n");
        sb.append("- 잘 드러난 경험·성과·기술 키워드를 bullet으로 3개 이내.\n\n");
        sb.append("### 3. 부족한 부분 (가장 중요)\n");
        sb.append("- 누락된 정보, 모호한 표현, 정량 지표 부재, 직무와의 관련성 부족 등을 ");
        sb.append("구체적인 문장 인용과 함께 bullet으로 지적. 최소 3개.\n\n");
        sb.append("### 4. 항목별 개선 제안\n");
        sb.append("- 자기소개/경력/프로젝트/기술스택/학력 등 발견되는 섹션별로 ");
        sb.append("\"현재 → 이렇게 바꾸세요\" 형식으로 제안.\n\n");
        sb.append("### 5. 우선순위 액션 3가지\n");
        sb.append("이력서를 다시 다듬을 때 가장 먼저 고쳐야 할 일 3개를 번호 매겨 정리.\n\n");
        // 마무리 가드레일: 언어(한국어), 사실 근거(환각 방지), 톤(칭찬보다 개선점 위주)을 마지막에 재강조.
        sb.append("- 모든 답변은 한국어.\n");
        sb.append("- 추측이 아닌 이력서에 실제 적힌 내용에 근거해서 평가할 것.\n");
        sb.append("- 칭찬보다 부족한 점을 더 자세히 짚을 것.\n");
        return sb.toString();
    }
}
