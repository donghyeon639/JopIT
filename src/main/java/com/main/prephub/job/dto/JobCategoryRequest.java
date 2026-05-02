package com.main.prephub.job.dto;

import com.main.prephub.job.JobCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobCategoryRequest {

    private String name;

    public JobCategory toEntity() {
        return JobCategory.builder()
                .name(name)
                .build();
    }
}