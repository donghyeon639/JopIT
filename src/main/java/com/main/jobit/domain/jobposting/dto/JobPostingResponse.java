package com.main.jobit.domain.jobposting.dto;

import com.main.jobit.domain.jobposting.JobPosting;
import com.main.jobit.domain.jobposting.JobSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// 채용 공고 목록/상세 응답 DTO.
// 엔티티(JobPosting)의 내부용 필드(rawPayload 원문 JSON, fetchedAt, jobCategory, active 등)는 노출하지 않고
// 화면에 필요한 필드만 추려 전달한다.
@Getter
@Builder
public class JobPostingResponse {

    private final UUID id;
    private final JobSource source;          // 출처(사람인/공공데이터 등)
    private final String externalId;         // 원본 소스에서의 공고 식별자
    private final String title;
    private final String company;
    private final String location;
    private final String careerLevel;        // 경력 구분
    private final String employmentType;     // 고용 형태
    private final String salaryRange;        // 급여 범위
    private final LocalDateTime postedAt;     // 게시일(목록 기본 정렬 기준)
    private final LocalDateTime expiresAt;    // 마감일
    private final String applyUrl;           // 지원 페이지 링크

    // 엔티티 → 응답 DTO 정적 팩터리. Page.map(JobPostingResponse::from) 형태로 페이징 변환에 쓰인다.
    public static JobPostingResponse from(JobPosting jp) {
        return JobPostingResponse.builder()
                .id(jp.getId())
                .source(jp.getSource())
                .externalId(jp.getExternalId())
                .title(jp.getTitle())
                .company(jp.getCompany())
                .location(jp.getLocation())
                .careerLevel(jp.getCareerLevel())
                .employmentType(jp.getEmploymentType())
                .salaryRange(jp.getSalaryRange())
                .postedAt(jp.getPostedAt())
                .expiresAt(jp.getExpiresAt())
                .applyUrl(jp.getApplyUrl())
                .build();
    }
}