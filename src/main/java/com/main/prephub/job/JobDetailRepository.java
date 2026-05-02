package com.main.prephub.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobDetailRepository extends JpaRepository<JobDetail, UUID> {
    List<JobDetail> findByCategoryId(UUID categoryId);
}
