package com.main.jobit.domain.jobposting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {

    Optional<JobPosting> findBySourceAndExternalId(JobSource source, String externalId);

    Page<JobPosting> findByActiveTrue(Pageable pageable);

    Page<JobPosting> findBySourceAndActiveTrue(JobSource source, Pageable pageable);
}