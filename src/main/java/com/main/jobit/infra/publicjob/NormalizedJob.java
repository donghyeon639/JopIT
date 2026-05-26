package com.main.jobit.infra.publicjob;

import java.time.LocalDateTime;

/**
 * 외부 채용 API 응답을 도메인 친화 형태로 정규화한 DTO.
 * 모든 소스 어댑터는 자신만의 응답 스키마를 이 record로 변환해서 넘긴다.
 */
public record NormalizedJob(
        String externalId,
        String title,
        String company,
        String location,
        String careerLevel,
        String employmentType,
        String salaryRange,
        LocalDateTime postedAt,
        LocalDateTime expiresAt,
        String applyUrl
) {}