package com.main.prephub.job.dto;

import com.main.prephub.job.JobCategory;
import com.main.prephub.job.JobDetail;
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