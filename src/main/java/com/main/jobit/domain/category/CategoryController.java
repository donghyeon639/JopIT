package com.main.jobit.domain.category;

import com.main.jobit.domain.category.QuestionCategoryRepository;
import com.main.jobit.domain.category.dto.QuestionCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 일반 사용자용 문제 카테고리 조회 API(인증 불필요한 공개 조회 성격).
// 관리자용 CRUD는 별도의 AdminQuestionCategoryController(@Admin)에 있다.
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final QuestionCategoryRepository questionCategoryRepository;

    // 문제 카테고리 전체 목록 조회. 마스터 데이터라 페이징 없이 전체 반환.
    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponse>> list() {
        return ResponseEntity.ok(
                questionCategoryRepository.findAll().stream()
                        .map(QuestionCategoryResponse::from)
                        .toList()
        );
    }
}