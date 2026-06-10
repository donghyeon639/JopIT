package com.main.jobit.ai.feedback;

import com.main.jobit.ai.port.LlmPort;
import com.main.jobit.domain.answer.Answer;
import com.main.jobit.domain.answer.AnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 사용자 답변에 대한 AI 피드백을 비동기로 생성하는 애플리케이션 서비스.
// 도메인은 LlmPort(포트)만 의존하고 구체 어댑터(BedrockLlmService)는 모른다 — 포트-어댑터 경계.
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    private final LlmPort llmPort;              // LLM 추상화 포트. 어댑터 교체 시에도 이 코드는 불변.
    private final AnswerRepository answerRepository;

    // AI 피드백 요청을 별도 스레드에서 처리한다.
    // @Async — LLM 호출은 수 초~수십 초 걸리므로 호출자(답변 저장 API)를 블로킹하지 않는다.
    //          AsyncConfig의 @EnableAsync가 활성화돼 있어야 실제로 별도 스레드에서 돈다.
    // @Transactional — 비동기 스레드 안에서 독립적인 트랜잭션/영속성 컨텍스트를 연다.
    //                  (호출자 트랜잭션은 이미 커밋·종료됐을 수 있으므로 여기서 새로 시작해야 함)
    @Async
    @Transactional
    public void requestFeedback(UUID answerId) {
        // 비동기 실행 시점에 답변이 삭제됐을 수 있으므로 예외 대신 null 처리 후 조용히 종료.
        Answer answer = answerRepository.findById(answerId).orElse(null);
        if (answer == null) {
            log.warn("AI 피드백 요청 실패 — 답변을 찾을 수 없음: {}", answerId);
            return;
        }

        // 먼저 상태를 PENDING으로 표시·저장해서 클라이언트가 폴링으로 "생성 중"을 인지하게 한다.
        answer.markFeedbackPending();
        answerRepository.save(answer);

        String prompt = buildPrompt(answer);

        try {
            // 실제 LLM 호출. 성공 시 피드백 본문과 함께 상태를 완료로 전환.
            String feedback = llmPort.generate(prompt);
            answer.applyFeedback(feedback);
        } catch (Exception e) {
            // LLM 타임아웃·비정상 종료 등 실패는 삼키고 FAILED로 마킹 — 비동기 스레드라 예외를 던져도 받을 곳이 없다.
            log.error("AI 피드백 생성 실패 (answerId={}): {}", answerId, e.getMessage());
            answer.markFeedbackFailed();
        }

        // 성공/실패 어느 경우든 최종 상태를 영속화.
        answerRepository.save(answer);
    }

    // 답변 평가용 프롬프트를 조립한다. 면접 질문 + (있으면) 모범 답안 + 지원자 답변을 한 덩어리로 묶어
    // 기술 면접 코치 역할을 부여하고, 한국어 마크다운 피드백을 요청한다.

    private String buildPrompt(Answer answer) {
        String questionTitle = answer.getQuestion().getTitle();
        String modelAnswer = answer.getQuestion().getModelAnswer();
        String userAnswer = answer.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 기술 면접 코치입니다. 아래 면접 질문에 대한 지원자의 답변을 평가해주세요.\n\n");
        sb.append("## 면접 질문\n").append(questionTitle).append("\n\n");
        // 모범 답안은 선택적 — 등록돼 있을 때만 참고용으로 프롬프트에 포함.
        if (modelAnswer != null && !modelAnswer.isBlank()) {
            sb.append("## 모범 답안 (참고용)\n").append(modelAnswer).append("\n\n");
        }
        sb.append("## 지원자 답변\n").append(userAnswer).append("\n\n");
        sb.append("## 평가 요청\n");
        sb.append("다음 기준으로 한국어로 피드백을 작성해주세요:\n");
        sb.append("1. **핵심 개념 이해도** — 답변이 질문의 핵심을 올바르게 짚었는지\n");
        sb.append("2. **정확성** — 기술적으로 틀린 내용이 있다면 지적\n");
        sb.append("3. **보완할 점** — 더 좋은 답변이 되기 위해 추가하면 좋을 내용\n");
        sb.append("4. **총평** — 한 두 문장으로 전체 평가 요약\n\n");
        sb.append("마크다운 형식으로 작성해주세요.");
        return sb.toString();
    }
}