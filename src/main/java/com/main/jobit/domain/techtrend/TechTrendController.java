package com.main.jobit.domain.techtrend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tech-trends")
@RequiredArgsConstructor
public class TechTrendController {

    private final TechTrendService techTrendService;

    @GetMapping
    public ResponseEntity<List<TechArticleDto>> list() {
        return ResponseEntity.ok(techTrendService.getLatest());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechArticleDto> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(techTrendService.getById(id));
    }
}