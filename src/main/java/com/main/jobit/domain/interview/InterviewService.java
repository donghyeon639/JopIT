package com.main.jobit.domain.interview;

import com.main.jobit.domain.interview.dto.AnswerSubmitRequest;
import com.main.jobit.domain.interview.dto.InterviewQuestionResponse;
import com.main.jobit.domain.interview.dto.InterviewSessionResponse;
import com.main.jobit.domain.interview.dto.InterviewSessionSummaryResponse;
import com.main.jobit.domain.job.JobCategory;
import com.main.jobit.domain.job.JobCategoryRepository;
import com.main.jobit.domain.resume.ResumeTextExtractor;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 면접 도메인의 동기 흐름(검증·권한·상태 전이)을 담당하는 서비스.
// LLM 호출 같은 느린/비동기 작업은 InterviewAiService에 위임하고, 여기선 트리거만 한다.
// 본인 소유 검증은 모든 진입 메서드가 공통으로 수행한다.
@Service
@RequiredArgsConstructor
public class InterviewService {

    // 이력서 본문 길이 가드. 너무 짧으면 질문 생성이 빈약하고, 너무 길면 프롬프트 토큰 비용/한도가 문제된다.
    private static final int MIN_RESUME_CHARS = 50;
    private static final int MAX_RESUME_CHARS = 50_000;

    private final UserRepository userRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final ResumeTextExtractor resumeTextExtractor;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewAiService interviewAiService;

    /**
     * 면접 세션 생성. 이력서 텍스트 추출 → 세션 저장(커밋) → 비동기 질문 생성 트리거.
     * 메서드 자체는 트랜잭션이 아니라 {@code save}가 즉시 커밋되므로, 비동기 스레드가 세션 행을 안전하게 본다.
     */
    public InterviewSessionResponse createSession(String username, MultipartFile file,
                                                  String jobCategoryName, String interviewTypeRaw) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));

        if (jobCategoryName == null || jobCategoryName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "직군을 선택해주세요.");
        }
        JobCategory jobCategory = jobCategoryRepository.findByName(jobCategoryName.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 직군입니다: " + jobCategoryName));

        InterviewType interviewType = parseType(interviewTypeRaw);

        ResumeTextExtractor.Extracted extracted = resumeTextExtractor.extract(file);
        String resumeText = extracted.text();
        validateResumeLength(resumeText);

        InterviewSession session = sessionRepository.save(InterviewSession.builder()
                .user(user)
                .jobCategory(jobCategory)
                .interviewType(interviewType)
                .resumeText(resumeText)
                .build());

        interviewAiService.generateQuestions(session.getId());

        return toResponse(session, List.of());
    }

    /**
     * 답변 제출. 원자적 상태 전이(WAITING/EVAL_FAILED → EVAL_PENDING)로 중복 제출을 막고, 커밋 후 비동기 평가를 트리거.
     */
    public InterviewSessionResponse submitAnswer(String username, UUID sessionId,
                                                 UUID questionId, AnswerSubmitRequest request) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 면접만 진행할 수 있습니다.");
        }
        if (session.getStatus() == InterviewStatus.QUESTIONS_PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "질문이 아직 생성 중입니다. 잠시 후 다시 시도해주세요.");
        }
        if (session.getStatus() == InterviewStatus.QUESTIONS_FAILED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "질문 생성에 실패한 세션입니다.");
        }

        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다."));
        if (!question.getSession().getId().equals(sessionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 세션의 질문이 아닙니다.");
        }

        int updated = questionRepository.submitAnswerIfEligible(
                questionId, request.transcript().trim(), LocalDateTime.now());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 답변했거나 평가 중인 질문입니다.");
        }

        interviewAiService.evaluateAnswer(questionId);

        return getSession(username, sessionId);
    }

    /**
     * 면접 종료 — 모든 질문이 답변된 경우에만 비동기로 종합 피드백을 생성한다.
     */
    public InterviewSessionResponse completeSession(String username, UUID sessionId) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 면접만 종료할 수 있습니다.");
        }

        List<InterviewQuestion> questions = questionRepository.findBySessionIdOrderByOrderNoAsc(sessionId);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "생성된 질문이 없습니다.");
        }
        boolean anyUnanswered = questions.stream().anyMatch(q -> q.getStatus() == QuestionStatus.WAITING);
        if (anyUnanswered) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "아직 답변하지 않은 질문이 있습니다.");
        }

        interviewAiService.generateOverallFeedback(sessionId);

        return getSession(username, sessionId);
    }

    // 내 면접 기록 목록. 질문 본문 없이 메타만 요약 DTO로 변환(fetch join으로 N+1 회피).
    @Transactional(readOnly = true)
    public List<InterviewSessionSummaryResponse> getMySessions(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
        return sessionRepository.findByUserIdWithJobCategory(user.getId()).stream()
                .map(s -> new InterviewSessionSummaryResponse(
                        s.getId(),
                        s.getJobCategory().getName(),
                        s.getInterviewType().name(),
                        s.getInterviewType().label(),
                        s.getStatus().name(),
                        s.getCreatedAt()))
                .toList();
    }

    // 세션 단건 조회(질문/답변/평가/상태 포함). 프런트 폴링의 주 엔드포인트이며 본인 소유만 허용.
    @Transactional(readOnly = true)
    public InterviewSessionResponse getSession(String username, UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 면접만 조회할 수 있습니다.");
        }
        List<InterviewQuestion> questions = questionRepository.findBySessionIdOrderByOrderNoAsc(sessionId);
        return toResponse(session, questions);
    }

    // 요청 문자열을 InterviewType으로 변환. 대소문자/공백을 정규화하고, 매핑 실패는 잘못된 enum이 아니라 400으로 돌려준다.
    private InterviewType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "면접 종류를 선택해주세요.");
        }
        try {
            return InterviewType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 면접 종류입니다: " + raw);
        }
    }

    // 추출된 이력서 본문 길이 검증(MIN/MAX 가드). 파일은 받았으나 텍스트 추출이 빈약한 경우를 여기서 걸러낸다.
    private void validateResumeLength(String text) {
        if (text.length() < MIN_RESUME_CHARS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "이력서 내용이 너무 짧습니다. 최소 " + MIN_RESUME_CHARS + "자 이상 필요합니다.");
        }
        if (text.length() > MAX_RESUME_CHARS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "이력서 내용이 너무 깁니다. 최대 " + MAX_RESUME_CHARS + "자까지 지원합니다.");
        }
    }

    // 엔티티 → 응답 DTO 매핑 공통 헬퍼. interviewType은 식별자(name)와 표기용(label)을 함께 내려 화면 분기/표시를 분리한다.
    private InterviewSessionResponse toResponse(InterviewSession s, List<InterviewQuestion> questions) {
        List<InterviewQuestionResponse> qs = questions.stream()
                .map(q -> new InterviewQuestionResponse(
                        q.getId(), q.getOrderNo(), q.getContent(),
                        q.getStatus().name(), q.getTranscript(), q.getEvaluation()))
                .toList();
        return new InterviewSessionResponse(
                s.getId(),
                s.getJobCategory().getName(),
                s.getInterviewType().name(),
                s.getInterviewType().label(),
                s.getStatus().name(),
                s.getOverallFeedback(),
                s.getCreatedAt(),
                qs);
    }
}
