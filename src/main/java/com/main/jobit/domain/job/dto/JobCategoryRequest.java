package com.main.jobit.domain.job.dto;

import com.main.jobit.domain.job.JobCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 직군 생성 요청 바디 DTO. 관리자가 새 직군을 추가할 때 받는 입력.
// (참고: QuestionCategoryRequest와 달리 @NotBlank 검증이 걸려 있지 않아
//  빈 name도 통과할 수 있음 — 필요 시 검증 보강 여지 있음)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobCategoryRequest {

    private String name;

    // 요청 DTO → 엔티티 변환. 생성 시점이라 id/createdAt은 비운 채 name만 채운다.
    public JobCategory toEntity() {
        return JobCategory.builder()
                .name(name)
                .build();
    }
}
