package com.main.jobit.domain.techtrend;

import java.util.UUID;

// 프런트로 내려보내는 기술 트렌드 응답 DTO.
// 엔티티(TechArticle)를 그대로 노출하지 않고 화면에 필요한 필드만 추려 전달한다.
// publishedAt(LocalDateTime)은 그대로 주지 않고 "yyyy.MM.dd" 포맷 문자열(date)로 가공해 내려준다(toDto 참고).
public record TechArticleDto(
    UUID id,
    String title,
    String source,
    String description,  // 카드 미리보기용 (120자 plain text)
    String content,      // 상세 페이지용 (RSS full HTML)
    String tag,
    String url,
    String imageUrl,     // 썸네일
    String date          // 발행일 표시용 문자열("yyyy.MM.dd"). 원본 LocalDateTime을 포맷한 값
) {}