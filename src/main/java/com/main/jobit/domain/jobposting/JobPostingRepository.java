package com.main.jobit.domain.jobposting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// 채용 공고 영속성 레포지토리.
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {

    // 동기화 upsert 시 중복 판별 키. (source, externalId)는 UNIQUE라 기존 공고를 찾아 update/save 결정.
    Optional<JobPosting> findBySourceAndExternalId(JobSource source, String externalId);

    // 공개 목록(전체 소스). active=true(노출 중)인 공고만 페이징 조회.
    Page<JobPosting> findByActiveTrue(Pageable pageable);

    // 공개 목록(소스 필터). 특정 소스의 노출 중 공고만 페이징 조회.
    Page<JobPosting> findBySourceAndActiveTrue(JobSource source, Pageable pageable);
}