package com.main.jobit.domain.jobposting.dto;

import com.main.jobit.domain.jobposting.JobPosting;
import com.main.jobit.domain.jobposting.JobSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class JobPostingResponse {

    private final UUID id;
    private final JobSource source;
    private final String externalId;
    private final String title;
    private final String company;
    private final String location;
    private final String careerLevel;
    private final String employmentType;
    private final String salaryRange;
    private final LocalDateTime postedAt;
    private final LocalDateTime expiresAt;
    private final String applyUrl;

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