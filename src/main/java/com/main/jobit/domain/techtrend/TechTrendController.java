package com.main.jobit.domain.techtrend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// 기술 트렌드(기술 블로그 수집글) 공개 조회 API.
// 수집/동기화는 스케줄러(TechTrendService)가 담당하고, 이 컨트롤러는 읽기 전용 노출만 한다.
@RestController
@RequestMapping("/api/tech-trends")
@RequiredArgsConstructor
public class TechTrendController {

    private final TechTrendService techTrendService;

    // 대시보드용 최신 글 목록(최대 8건). 서비스 단에서 캐시된 결과를 반환한다.
    @GetMapping
    public ResponseEntity<List<TechArticleDto>> list() {
        return ResponseEntity.ok(techTrendService.getLatest());
    }

    // 상세 페이지용 단건 조회. 존재하지 않으면 서비스에서 IllegalArgumentException을 던진다.
    @GetMapping("/{id}")
    public ResponseEntity<TechArticleDto> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(techTrendService.getById(id));
    }
}