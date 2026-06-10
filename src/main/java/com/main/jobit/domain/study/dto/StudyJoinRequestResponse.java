package com.main.jobit.domain.study.dto;

import com.main.jobit.domain.study.JoinRequestStatus;
import com.main.jobit.domain.study.StudyJoinRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 참여 신청 응답 DTO. 작성자가 보는 신청자 목록/처리 결과에 사용된다.
 * applicantNickname을 함께 내려주기 위해 from()에서 applicant를 참조하므로,
 * 목록 조회 시 applicant가 JOIN FETCH로 미리 로딩되어 있어야 LazyInitializationException을 피한다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyJoinRequestResponse {

    private UUID id;
    private UUID studyId;
    private UUID applicantId;
    private String applicantNickname;
    private JoinRequestStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudyJoinRequestResponse(UUID id, UUID studyId, UUID applicantId, String applicantNickname,
                                    JoinRequestStatus status, String message,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studyId = studyId;
        this.applicantId = applicantId;
        this.applicantNickname = applicantNickname;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StudyJoinRequestResponse from(StudyJoinRequest r) {
        return StudyJoinRequestResponse.builder()
                .id(r.getId())
                .studyId(r.getStudy().getId())
                .applicantId(r.getApplicant().getId())
                .applicantNickname(r.getApplicant().getNickname())
                .status(r.getStatus())
                .message(r.getMessage())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
