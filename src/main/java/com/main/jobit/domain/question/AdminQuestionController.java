package com.main.jobit.domain.question;

import com.main.jobit.domain.question.dto.QuestionCreateRequest;
import com.main.jobit.domain.question.dto.QuestionDetailResponse;
import com.main.jobit.domain.question.dto.QuestionPagedResponse;
import com.main.jobit.domain.question.dto.QuestionSummaryResponse;
import com.main.jobit.global.security.Admin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 관리자 전용 문제 CRUD API. 클래스 레벨 @Admin AOP로 모든 핸들러가 관리자 권한을 요구한다.
// 조회 로직(list/detail)은 일반 컨트롤러와 동일한 QuestionService를 재사용한다.
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@Admin
public class AdminQuestionController {

    private final QuestionService questionService;

    // 관리자 목록 조회. 일반 화면(기본 10개)보다 큰 기본 20개로 둬 관리 작업 효율을 높임.
    @GetMapping
    public ResponseEntity<QuestionPagedResponse<QuestionSummaryResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(questionService.list(categoryId, difficulty, q, pageable));
    }

    // 단건 상세 조회(편집 폼 진입용).
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    // 문제 생성. @Valid로 요청 바디 검증 후 성공 시 201 Created.
    @PostMapping
    public ResponseEntity<QuestionDetailResponse> create(@RequestBody @Valid QuestionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(request));
    }

    // 문제 수정(전체 필드 교체). 생성과 동일한 요청 DTO를 재사용한다.
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid QuestionCreateRequest request) {
        return ResponseEntity.ok(questionService.update(id, request));
    }

    // 문제 삭제. 성공 시 본문 없이 204 No Content.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
