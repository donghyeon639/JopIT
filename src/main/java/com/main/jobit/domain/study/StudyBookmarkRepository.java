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

public interface StudyBookmarkRepository extends JpaRepository<StudyBookmark, UUID> {

    Optional<StudyBookmark> findByUserIdAndStudyId(UUID userId, UUID studyId);

    boolean existsByUserIdAndStudyId(UUID userId, UUID studyId);

    Page<StudyBookmark> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

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
