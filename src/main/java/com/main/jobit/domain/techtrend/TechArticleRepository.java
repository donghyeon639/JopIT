package com.main.jobit.domain.techtrend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 기술 블로그 글 영속성 레포지토리.
public interface TechArticleRepository extends JpaRepository<TechArticle, UUID> {

    // upsert 시 중복 판별용. url(UNIQUE)로 기존 글을 찾아 있으면 update, 없으면 save 한다.
    Optional<TechArticle> findByUrl(String url);

    // 목록 화면용: 발행일 최신순 상위 8건만 조회. getLatest()에서 사용하며 결과는 캐시된다.
    List<TechArticle> findTop8ByOrderByPublishedAtDesc();
}