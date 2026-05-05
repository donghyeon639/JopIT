package com.main.prephub.answer;

import com.main.prephub.aifeedback.AiFeedbackService;
import com.main.prephub.answer.dto.AnswerCreateRequest;
import com.main.prephub.answer.dto.AnswerResponse;
import com.main.prephub.comment.CommentRepository;
import com.main.prephub.question.Question;
import com.main.prephub.question.QuestionRepository;
import com.main.prephub.user.UserRepository;
import com.main.prephub.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AiFeedbackService aiFeedbackService;

    @Transactional
    public AnswerResponse create(UUID questionId, String username, AnswerCreateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다."));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(request.getContent())
                .build();

        answerRepository.save(answer);
        return AnswerResponse.from(answer);
    }

    @Transactional(readOnly = true)
    public AnswerResponse getById(UUID answerId) {
        Answer answer = findAnswer(answerId);
        return AnswerResponse.from(answer, commentRepository.countByAnswerId(answer.getId()));
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> getByQuestion(UUID questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId)
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> getCommunityFeed() {
        return answerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> getMyAnswers(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return answerRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    @Transactional
    public AnswerResponse requestFeedback(UUID answerId, String username) {
        Answer answer = findAnswer(answerId);

        if (!answer.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 답변에만 AI 피드백을 요청할 수 있습니다.");
        }
        if (answer.getFeedbackStatus() == FeedbackStatus.PENDING) {
            throw new IllegalArgumentException("이미 피드백 생성 중입니다.");
        }
        if (answer.getFeedbackStatus() == FeedbackStatus.DONE) {
            throw new IllegalArgumentException("이미 피드백이 완료된 답변입니다.");
        }

        aiFeedbackService.requestFeedback(answerId);
        return AnswerResponse.from(answer);
    }

    private Answer findAnswer(UUID answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));
    }
}