package com.main.jobit.domain.study;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * 목록 조회용 동적 필터 모음. 각 메서드는 하나의 검색 조건을 Specification으로 표현한다.
 *
 * 핵심 규약: 인자가 null/blank면 toPredicate가 null을 반환한다.
 *   → Specification.allOf(...)로 합성할 때 null 조건은 자동으로 무시되므로,
 *     Service에서 if 분기로 조건을 조립할 필요 없이 모든 필터를 한 줄에 나열할 수 있다.
 *
 * ElementCollection(techStacks/positions) 필터는 컬렉션 테이블을 JOIN하므로,
 *   한 스터디가 여러 태그를 가지면 결과 행이 중복될 수 있다 → query.distinct(true)로 방지.
 *
 * 유틸 클래스 — 생성자 private으로 인스턴스화 차단.
 */
public final class StudySpecifications {

    private StudySpecifications() {}

    // 유형(STUDY/PROJECT) 동등 필터. null이면 조건 미적용.
    public static Specification<Study> typeEquals(StudyType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    // 진행 방식(ONLINE/OFFLINE/HYBRID) 동등 필터.
    public static Specification<Study> modeEquals(StudyMode mode) {
        return (root, query, cb) -> mode == null ? null : cb.equal(root.get("mode"), mode);
    }

    // 모집 상태 필터. "모집 중만 보기"는 Service에서 RECRUITING을 넘겨 사용.
    public static Specification<Study> statusEquals(StudyStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // 기술 스택 정확히 일치 필터. techStacks 컬렉션을 JOIN하고 distinct로 중복 행 제거.
    public static Specification<Study> techStackEquals(String techStack) {
        return (root, query, cb) -> {
            if (techStack == null || techStack.isBlank()) return null;
            if (query != null) query.distinct(true);   // 컬렉션 JOIN으로 인한 중복 행 방지
            return cb.equal(root.join("techStacks"), techStack);
        };
    }

    // 포지션 정확히 일치 필터. 구조는 techStackEquals와 동일.
    public static Specification<Study> positionEquals(String position) {
        return (root, query, cb) -> {
            if (position == null || position.isBlank()) return null;
            if (query != null) query.distinct(true);
            return cb.equal(root.join("positions"), position);
        };
    }

    // 제목 또는 소개에 키워드 포함(부분 일치) 검색. 양쪽을 lower로 맞춰 대소문자 구분 없이 LIKE.
    public static Specification<Study> titleOrSummaryContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("summary")), like)
            );
        };
    }

    /**
     * 특정 사용자가 북마크한 스터디로 한정. userId가 null이면 조건 없음.
     *
     * 구현 방식: StudyBookmark를 직접 JOIN하지 않고 "내가 북마크한 study id" 서브쿼리에 IN.
     *   → 메인 쿼리에 북마크 테이블 조인을 끌어들이지 않아 행 중복/정렬 영향이 없고,
     *     "북마크만 보기" 필터를 다른 필터와 독립적으로 합성할 수 있다.
     */
    public static Specification<Study> bookmarkedBy(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null || query == null) return null;
            Subquery<UUID> sq = query.subquery(UUID.class);
            Root<StudyBookmark> bm = sq.from(StudyBookmark.class);
            sq.select(bm.get("study").get("id"))                 // 이 사용자가 북마크한 study id 집합
              .where(cb.equal(bm.get("user").get("id"), userId));
            return root.get("id").in(sq);                        // 메인 study.id가 그 집합에 속하면 통과
        };
    }
}
