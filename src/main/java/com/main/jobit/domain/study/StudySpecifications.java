package com.main.jobit.domain.study;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * 목록 조회용 동적 필터 모음. null/blank이면 조건을 추가하지 않는다.
 * ElementCollection(techStacks/positions) 조인은 query.distinct(true)로 중복 행을 방지한다.
 */
public final class StudySpecifications {

    private StudySpecifications() {}

    public static Specification<Study> typeEquals(StudyType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Study> modeEquals(StudyMode mode) {
        return (root, query, cb) -> mode == null ? null : cb.equal(root.get("mode"), mode);
    }

    public static Specification<Study> statusEquals(StudyStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Study> techStackEquals(String techStack) {
        return (root, query, cb) -> {
            if (techStack == null || techStack.isBlank()) return null;
            if (query != null) query.distinct(true);
            return cb.equal(root.join("techStacks"), techStack);
        };
    }

    public static Specification<Study> positionEquals(String position) {
        return (root, query, cb) -> {
            if (position == null || position.isBlank()) return null;
            if (query != null) query.distinct(true);
            return cb.equal(root.join("positions"), position);
        };
    }

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

    /** 특정 사용자가 북마크한 스터디로 한정. userId가 null이면 조건 없음. */
    public static Specification<Study> bookmarkedBy(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null || query == null) return null;
            Subquery<UUID> sq = query.subquery(UUID.class);
            Root<StudyBookmark> bm = sq.from(StudyBookmark.class);
            sq.select(bm.get("study").get("id"))
              .where(cb.equal(bm.get("user").get("id"), userId));
            return root.get("id").in(sq);
        };
    }
}
