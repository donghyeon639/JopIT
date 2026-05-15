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

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    private final LlmPort llmPort;
    private final AnswerRepository answerRepository;

    @Async
    @Transactional
    public void requestFeedback(UUID answerId) {
        Answer answer = answerRepository.findById(answerId).orElse(null);
        if (answer == null) {
            log.warn("AI 피드백 요청 실패 — 답변을 찾을 수 없음: {}", answerId);
            return;
        }

        answer.markFeedbackPending();
        answerRepository.save(answer);

        String prompt = buildPrompt(answer);

        try {
            String feedback = llmPort.generate(prompt);
            answer.applyFeedback(feedback);
        } catch (Exception e) {
            log.error("AI 피드백 생성 실패 (answerId={}): {}", answerId, e.getMessage());
            answer.markFeedbackFailed();
        }

        answerRepository.save(answer);
    }

    private String buildPrompt(Answer answer) {
        String questionTitle = answer.getQuestion().getTitle();
        String modelAnswer = answer.getQuestion().getModelAnswer();
        String userAnswer = answer.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 기술 면접 코치입니다. 아래 면접 질문에 대한 지원자의 답변을 평가해주세요.\n\n");
        sb.append("## 면접 질문\n").append(questionTitle).append("\n\n");
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