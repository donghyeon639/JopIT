package com.main.jobit.domain.answer;

import com.main.jobit.ai.feedback.AiFeedbackService;
import com.main.jobit.domain.answer.dto.AnswerCreateRequest;
import com.main.jobit.domain.answer.dto.AnswerResponse;
import com.main.jobit.domain.comment.CommentRepository;
import com.main.jobit.domain.question.Question;
import com.main.jobit.domain.question.QuestionRepository;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

// 답변 생성/조회와 AI 피드백 요청 트리거를 담당하는 애플리케이션 서비스.
// 조회 메서드는 댓글 수(commentCount)를 함께 묶어 응답 DTO 로 변환하며,
// 피드백 요청은 상태 검증 → 비동기 위임 순으로 처리한다.
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;   // 답변별 댓글 수 집계에 사용
    private final AiFeedbackService aiFeedbackService;    // 비동기 AI 피드백 실행을 위임할 포트성 서비스

    // 답변 작성. 질문/사용자 존재를 먼저 검증한 뒤 새 답변을 저장한다.
    @Transactional
    public AnswerResponse create(UUID questionId, String username, AnswerCreateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 문제입니다."));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));

        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(request.getContent())
                .build();

        answerRepository.save(answer);
        // 방금 생성된 답변에는 댓글이 없으므로 댓글 수 없이 변환(commentCount = null).
        return AnswerResponse.from(answer);
    }

    // 답변 단건 조회. 상세 화면용으로 댓글 수까지 함께 채워 반환.
    @Transactional(readOnly = true)
    public AnswerResponse getById(UUID answerId) {
        Answer answer = findAnswer(answerId);
        return AnswerResponse.from(answer, commentRepository.countByAnswerId(answer.getId()));
    }

    // 특정 질문의 답변 목록(최신순). 각 답변마다 댓글 수를 개별 집계해 매핑한다.
    // NOTE: 목록 길이만큼 댓글 수 쿼리가 나가므로(N+1) 답변이 많아지면 일괄 집계로 개선 여지 있음.
    @Transactional(readOnly = true)
    public List<AnswerResponse> getByQuestion(UUID questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId)
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    // 커뮤니티 피드: 전체 답변 최신순. (위와 동일하게 댓글 수 N+1 주의)
    @Transactional(readOnly = true)
    public List<AnswerResponse> getCommunityFeed() {
        return answerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    // 마이페이지: 로그인 사용자가 작성한 답변 목록.
    @Transactional(readOnly = true)
    public List<AnswerResponse> getMyAnswers(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));
        return answerRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(a -> AnswerResponse.from(a, commentRepository.countByAnswerId(a.getId())))
                .toList();
    }

    // AI 피드백 요청 진입점. 동기 구간에서는 "권한 확인 + 상태 선점"만 하고,
    // 실제 LLM 호출은 aiFeedbackService 에 비동기로 넘긴다(응답 지연 차단).
    @Transactional
    public AnswerResponse requestFeedback(UUID answerId, String username) {
        Answer answer = findAnswer(answerId);

        // 본인 답변에만 피드백 허용. 타인 답변에 대한 무분별한 LLM 비용 발생 방지.
        if (!answer.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 답변에만 AI 피드백을 요청할 수 있습니다.");
        }

        // 상태를 조건부로 PENDING 선점(NONE/FAILED 일 때만). 영향 행이 0 이면
        // 이미 진행 중(PENDING)이거나 완료(DONE)된 것이므로 중복 요청을 409 로 거절한다.
        int updated = answerRepository.markFeedbackPendingIfEligible(answerId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 피드백이 진행 중이거나 완료된 답변입니다.");
        }

        // 실제 피드백 생성은 비동기로 위임. 트랜잭션 커밋 이후 별도 스레드에서 진행된다.
        aiFeedbackService.requestFeedback(answerId);

        // PENDING 으로 선점된 최신 상태를 다시 읽어 응답한다(벌크 업데이트로 클리어된 컨텍스트 재조회).
        return AnswerResponse.from(findAnswer(answerId));
    }

    // 답변 조회 공통 헬퍼. 없으면 404.
    private Answer findAnswer(UUID answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 답변입니다."));
    }
}