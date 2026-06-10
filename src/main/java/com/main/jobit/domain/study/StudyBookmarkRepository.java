package com.main.jobit.domain.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 스터디 북마크 영속성. (user, study) 단위 조회/존재확인/삭제와 내 북마크 목록 조회를 제공.
public interface StudyBookmarkRepository extends JpaRepository<StudyBookmark, UUID> {

    // 토글 시 기존 북마크 존재 여부 + 삭제 대상 엔티티를 한 번에 얻기 위해 Optional로 조회.
    Optional<StudyBookmark> findByUserIdAndStudyId(UUID userId, UUID studyId);

    // 상세 화면에서 "이 스터디를 내가 북마크했는지"만 빠르게 확인할 때 사용(엔티티 로딩 불필요).
    boolean existsByUserIdAndStudyId(UUID userId, UUID studyId);

    // 내 북마크 목록 — 최신 북마크 순으로 페이징. (user_id,created_at) 인덱스를 그대로 활용.
    Page<StudyBookmark> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // 파생 삭제 쿼리. 현재 토글 로직은 findBy...로 조회 후 delete(entity)를 사용하므로 보조 경로로 유지.
    void deleteByUserIdAndStudyId(UUID userId, UUID studyId);

    /**
     * 목록 화면용 — 현재 사용자가 북마크한 스터디 ID들을 한 번에 조회.
     * 카드마다 개별 EXISTS 호출하는 N+1을 방지.
     */
    @Query("""
            SELECT b.study.id
              FROM StudyBookmark b
             WHERE b.user.id = :userId AND b.study.id IN :studyIds
            """)
    List<UUID> findBookmarkedStudyIds(@Param("userId") UUID userId,
                                     @Param("studyIds") Collection<UUID> studyIds);
}
