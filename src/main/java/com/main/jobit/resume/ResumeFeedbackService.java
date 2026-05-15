package com.main.jobit.resume;

import com.main.jobit.aifeedback.LlmPort;
import com.main.jobit.resume.dto.ResumeFeedbackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeFeedbackService {

    private static final int MIN_CHARS = 50;
    private static final int MAX_CHARS = 50_000;

    private final LlmPort llmPort;
    private final ResumeTextExtractor extractor;

    public ResumeFeedbackResponse fromText(String text, String jobCategory) {
        String cleaned = text == null ? "" : text.trim();
        validateLength(cleaned);
        String feedback = llmPort.generate(buildPrompt(cleaned, jobCategory));
        return new ResumeFeedbackResponse(feedback, cleaned.length(), "text/plain");
    }

    public ResumeFeedbackResponse fromFile(MultipartFile file, String jobCategory) {
        ResumeTextExtractor.Extracted extracted = extractor.extract(file);
        String text = extracted.text();
        validateLength(text);
        String feedback = llmPort.generate(buildPrompt(text, jobCategory));
        return new ResumeFeedbackResponse(feedback, text.length(), extracted.mimeType());
    }

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

    private String buildPrompt(String resumeText, String jobCategory) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 IT 채용 시장에 정통한 시니어 이력서 첨삭 코치입니다. ");
        sb.append("아래 이력서를 검토하고, 어떤 부분이 부족한지 구체적으로 짚어주세요.\n\n");

        if (jobCategory != null && !jobCategory.isBlank()) {
            sb.append("## 지원자가 목표하는 직군\n").append(jobCategory).append("\n\n");
        }

        sb.append("## 이력서 원문\n");
        sb.append("```\n").append(resumeText).append("\n```\n\n");

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
        sb.append("- 모든 답변은 한국어.\n");
        sb.append("- 추측이 아닌 이력서에 실제 적힌 내용에 근거해서 평가할 것.\n");
        sb.append("- 칭찬보다 부족한 점을 더 자세히 짚을 것.\n");
        return sb.toString();
    }
}
