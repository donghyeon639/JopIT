package com.main.prephub.admin;

import com.main.prephub.question.QuestionCategory;
import com.main.prephub.question.QuestionCategoryRepository;
import com.main.prephub.question.dto.QuestionCategoryRequest;
import com.main.prephub.question.dto.QuestionCategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/question-categories")
@RequiredArgsConstructor
public class AdminQuestionCategoryController {

    private final QuestionCategoryRepository questionCategoryRepository;
    //문제 카테고리 조회
    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponse>> list() {
        return ResponseEntity.ok(
                questionCategoryRepository.findAll().stream()
                        .map(QuestionCategoryResponse::from)
                        .toList()
        );
    }
    // 문제 카테고리 추가
    @PostMapping
    public ResponseEntity<QuestionCategoryResponse> create(@RequestBody @Valid QuestionCategoryRequest request) {
        if (questionCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
        }
        QuestionCategory saved = questionCategoryRepository.save(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionCategoryResponse.from(saved));
    }

    // 문제 카테고리 이름 수정
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<QuestionCategoryResponse> update(@PathVariable UUID id, @RequestBody @Valid QuestionCategoryRequest request) {
        QuestionCategory category = questionCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        questionCategoryRepository.findByName(request.getName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new IllegalArgumentException("이미 사용 중인 카테고리명입니다."); });

        category.changeName(request.getName());
        return ResponseEntity.ok(QuestionCategoryResponse.from(category));
    }

    // 문제 카테고리 제거
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}