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

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<QuestionPagedResponse<QuestionSummaryResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Difficulty difficulty,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(questionService.list(categoryId, difficulty, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }
}