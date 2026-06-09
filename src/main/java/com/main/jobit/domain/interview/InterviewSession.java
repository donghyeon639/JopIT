package com.main.jobit.domain.interview;

import com.main.jobit.domain.job.JobCategory;
import com.main.jobit.domain.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 면접 회차. 사용자가 이력서 + 직무 + 면접 종류를 선택해 생성한다.
 * 이력서 본문은 생성 시점 스냅샷({@code resumeText})으로 보관해 이후 이력서가 바뀌어도 면접 컨텍스트가 고정된다.
 */
@Entity
@Table(name = "interview_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession {

    // PK는 UUID. 면접 세션 URL/식별자가 순번으로 추측되지 않도록 시퀀스 대신 UUID를 쓴다.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 세션 소유자. 조회/제출/종료 시 본인 검증의 기준이 된다. 목록 조회 외엔 본문이 필요 없어 LAZY.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 면접 대상 직군. 질문/평가 프롬프트의 "~ 직무 면접관입니다" 컨텍스트로 쓰인다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    // 인성/심층 구분. enum 이름을 그대로 문자열로 저장(EnumType.STRING)해 순서 변경에 안전하게 한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 20)
    private InterviewType interviewType;

    // 생성 시점 이력서 본문 스냅샷. 원본 이력서가 나중에 수정/삭제돼도 이 면접의 질문 맥락은 고정 유지된다.
    @Column(name = "resume_text", nullable = false, columnDefinition = "TEXT")
    private String resumeText;

    // 세션 상태 머신. 비동기 질문 생성·종합 피드백 진행도를 나타낸다(InterviewStatus 참고).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status;

    // 면접 종료 후 LLM이 만든 종합 총평(마크다운). 종료 전엔 null이며 비동기로 뒤늦게 채워진다.
    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    // 생성 시각. @PrePersist에서 한 번 세팅하고 updatable=false로 이후 변경을 막는다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성 전용 빌더. status는 외부에서 받지 않고 항상 QUESTIONS_PENDING으로 시작하도록 강제한다.
    @Builder
    public InterviewSession(Users user, JobCategory jobCategory, InterviewType interviewType, String resumeText) {
        this.user = user;
        this.jobCategory = jobCategory;
        this.interviewType = interviewType;
        this.resumeText = resumeText;
        this.status = InterviewStatus.QUESTIONS_PENDING;
    }

    // 저장 직전 기본값 보정. 빌더를 거치지 않고 영속화되는 경로(테스트 등)에서도 불변식이 깨지지 않게 방어한다.
    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = InterviewStatus.QUESTIONS_PENDING;
    }

    // 비동기 질문 생성 성공 시 호출 — PENDING → READY로 전이해 답변 제출을 허용한다.
    public void markQuestionsReady() {
        this.status = InterviewStatus.QUESTIONS_READY;
    }

    // 질문 생성 실패 시 호출 — PENDING에 영구 고착되지 않도록 명시적 실패 상태로 종료시킨다.
    public void markQuestionsFailed() {
        this.status = InterviewStatus.QUESTIONS_FAILED;
    }

    // 종합 피드백 도착 시 호출 — 본문을 채우면서 동시에 COMPLETED로 전이(한 동작으로 묶어 일관성 유지).
    public void applyOverallFeedback(String feedback) {
        this.overallFeedback = feedback;
        this.status = InterviewStatus.COMPLETED;
    }
}
