package com.main.jobit.domain.question;

import com.main.jobit.domain.question.dto.QuestionDetailResponse;
import com.main.jobit.domain.question.dto.QuestionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionSummaryResponse>> list(
            @RequestParam(required = false) UUID categoryId) {
        List<QuestionSummaryResponse> result = categoryId != null
                ? questionService.getByCategory(categoryId)
                : questionService.getAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }
}