package com.main.jobit.domain.question;

import com.main.jobit.domain.question.dto.QuestionDetailResponse;
import com.main.jobit.domain.question.dto.QuestionPagedResponse;
import com.main.jobit.domain.question.dto.QuestionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 일반 사용자용 문제 조회 API(목록/상세). 생성·수정·삭제는 관리자 컨트롤러에만 존재한다.
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // 문제 목록 조회. 카테고리/난이도/검색어(q)는 모두 선택 파라미터.
    // @PageableDefault로 기본 10개, 최신순(createdAt DESC) — 정렬/크기는 서비스의 sanitize에서 한 번 더 검증됨.
    @GetMapping
    public ResponseEntity<QuestionPagedResponse<QuestionSummaryResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(questionService.list(categoryId, difficulty, q, pageable));
    }

    // 문제 단건 상세 조회(힌트/모범답안 포함). 없으면 서비스에서 예외 → 전역 핸들러가 처리.
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }
}