package com.main.jobit.infra.publicjob;

import java.time.LocalDateTime;

/**
 * 외부 채용 API 응답을 도메인 친화 형태로 정규화한 DTO.
 * 모든 소스 어댑터는 자신만의 응답 스키마를 이 record로 변환해서 넘긴다.
 */
public record NormalizedJob(
        String externalId,        // 소스별 고유 식별자. 중복 적재 방지·업서트 키로 사용.
        String title,             // 공고 제목
        String company,           // 기관/회사명
        String location,          // 근무 지역
        String careerLevel,       // 경력 구분(신입/경력 등)
        String employmentType,    // 고용 형태(정규직/계약직 등) — 코드는 어댑터에서 한글로 디코드
        String salaryRange,       // 급여 정보(소스에 없으면 null)
        LocalDateTime postedAt,   // 공고 시작일
        LocalDateTime expiresAt,  // 공고 마감일
        String applyUrl           // 지원/원문 링크
) {}