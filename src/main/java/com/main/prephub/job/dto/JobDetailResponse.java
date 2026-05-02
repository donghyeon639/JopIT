package com.main.prephub.job.dto;

import com.main.prephub.job.JobDetail;
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
public class JobDetailResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private LocalDateTime createdAt;

    public static JobDetailResponse from(JobDetail detail) {
        return JobDetailResponse.builder()
                .id(detail.getId())
                .categoryId(detail.getCategory().getId())
                .categoryName(detail.getCategory().getName())
                .name(detail.getName())
                .createdAt(detail.getCreatedAt())
                .build();
    }
}