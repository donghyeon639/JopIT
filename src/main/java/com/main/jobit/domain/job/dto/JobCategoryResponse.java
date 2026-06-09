package com.main.jobit.domain.job.dto;

import com.main.jobit.domain.job.JobCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// JobCategory(직군) 엔티티를 외부로 내보내기 위한 응답 DTO.
// 엔티티를 직접 노출하지 않고 필요한 필드만 추려 직렬화 → JPA 지연로딩/순환참조 회피.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobCategoryResponse {

    private UUID id;
    private String name;
    private LocalDateTime createdAt;

    // 엔티티 → DTO 변환 정적 팩토리. 컨트롤러에서 map(JobCategoryResponse::from)으로 사용.
    public static JobCategoryResponse from(JobCategory category) {
        return JobCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}