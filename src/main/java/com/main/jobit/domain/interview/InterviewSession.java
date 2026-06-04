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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 20)
    private InterviewType interviewType;

    @Column(name = "resume_text", nullable = false, columnDefinition = "TEXT")
    private String resumeText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InterviewSession(Users user, JobCategory jobCategory, InterviewType interviewType, String resumeText) {
        this.user = user;
        this.jobCategory = jobCategory;
        this.interviewType = interviewType;
        this.resumeText = resumeText;
        this.status = InterviewStatus.QUESTIONS_PENDING;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = InterviewStatus.QUESTIONS_PENDING;
    }

    public void markQuestionsReady() {
        this.status = InterviewStatus.QUESTIONS_READY;
    }

    public void markQuestionsFailed() {
        this.status = InterviewStatus.QUESTIONS_FAILED;
    }

    public void applyOverallFeedback(String feedback) {
        this.overallFeedback = feedback;
        this.status = InterviewStatus.COMPLETED;
    }
}
