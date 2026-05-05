package com.main.jobit.job.dto;

import com.main.jobit.job.JobCategory;
import com.main.jobit.job.JobDetail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobDetailRequest {

    private UUID categoryId;
    private String name;

    public JobDetail toEntity(JobCategory category) {
        return JobDetail.builder()
                .category(category)
                .name(name)
                .build();
    }
}