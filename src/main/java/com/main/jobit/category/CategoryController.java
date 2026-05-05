package com.main.jobit.category;

import com.main.jobit.question.QuestionCategoryRepository;
import com.main.jobit.question.dto.QuestionCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final QuestionCategoryRepository questionCategoryRepository;

    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponse>> list() {
        return ResponseEntity.ok(
                questionCategoryRepository.findAll().stream()
                        .map(QuestionCategoryResponse::from)
                        .toList()
        );
    }
}