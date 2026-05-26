package com.main.jobit.domain.job.dto;

import com.main.jobit.domain.job.JobCategory;
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
