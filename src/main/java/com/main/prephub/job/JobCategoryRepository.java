package com.main.prephub.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobCategoryRepository extends JpaRepository<JobCategory, UUID> {
    Optional<JobCategory> findByName(String name);
}

