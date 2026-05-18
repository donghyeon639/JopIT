package com.main.jobit.domain.techtrend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TechArticleRepository extends JpaRepository<TechArticle, UUID> {

    Optional<TechArticle> findByUrl(String url);

    List<TechArticle> findTop8ByOrderByPublishedAtDesc();
}