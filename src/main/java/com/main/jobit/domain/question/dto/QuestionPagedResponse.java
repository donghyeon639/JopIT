package com.main.jobit.domain.question.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

// 페이지네이션 응답 래퍼(제네릭). Spring Data의 Page를 그대로 직렬화하지 않고
// 필요한 메타(현재 페이지/크기/총개수/총페이지/이전·다음 존재)만 추려 안정적인 응답 스키마로 고정한다.
// (Page 직렬화 포맷은 버전에 따라 바뀔 수 있어 프런트 계약을 지키려고 별도 DTO로 감싼 것)
@Getter
@Builder
public class QuestionPagedResponse<T> {

    private final List<T> content;       // 현재 페이지 항목들
    private final int page;              // 0-based 현재 페이지 번호
    private final int size;              // 페이지 크기
    private final long totalElements;    // 전체 항목 수
    private final int totalPages;        // 전체 페이지 수
    private final boolean hasNext;       // 다음 페이지 존재 여부
    private final boolean hasPrev;       // 이전 페이지 존재 여부

    // Spring Data Page → 응답 DTO 변환. content 타입 T는 이미 DTO로 매핑된 상태로 넘어온다.
    public static <T> QuestionPagedResponse<T> from(Page<T> page) {
        return QuestionPagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .build();
    }
}