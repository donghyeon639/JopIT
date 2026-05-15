package com.main.jobit.job.dto;

import com.main.jobit.job.JobCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobCategoryResponse {

    private UUID id;
    private String name;
    private LocalDateTime createdAt;

    public static JobCategoryResponse from(JobCategory category) {
        return JobCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}