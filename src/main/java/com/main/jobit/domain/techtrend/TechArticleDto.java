package com.main.jobit.domain.techtrend;

import java.util.UUID;

public record TechArticleDto(
    UUID id,
    String title,
    String source,
    String description,  // 카드 미리보기용 (120자 plain text)
    String content,      // 상세 페이지용 (RSS full HTML)
    String tag,
    String url,
    String imageUrl,     // 썸네일
    String date
) {}