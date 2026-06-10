package com.main.jobit.domain.category;

import com.main.jobit.domain.category.QuestionCategory;
import com.main.jobit.domain.category.QuestionCategoryRepository;
import com.main.jobit.domain.category.dto.QuestionCategoryRequest;
import com.main.jobit.domain.category.dto.QuestionCategoryResponse;
import com.main.jobit.global.security.Admin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 관리자 전용 문제 카테고리(QuestionCategory) CRUD API.
// 클래스 레벨 @Admin AOP로 모든 핸들러가 관리자 권한을 요구한다.
// 생성/수정에서 카테고리명 중복을 앱 단에서 선제적으로 막아 명확한 에러 메시지를 준다(unique 제약은 최후 방어선).
@RestController
@RequestMapping("/api/admin/question-categories")
@RequiredArgsConstructor
@Admin
public class AdminQuestionCategoryController {

    private final QuestionCategoryRepository questionCategoryRepository;
    //문제 카테고리 조회
    // 전체 목록 반환(마스터 데이터라 페이징 없음).
    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponse>> list() {
        return ResponseEntity.ok(
                questionCategoryRepository.findAll().stream()
                        .map(QuestionCategoryResponse::from)
                        .toList()
        );
    }
    // 문제 카테고리 추가
    // 생성 전 동일 name 존재 여부를 확인해 중복이면 거부, 통과 시 201로 저장 결과 반환.
    @PostMapping
    public ResponseEntity<QuestionCategoryResponse> create(@RequestBody @Valid QuestionCategoryRequest request) {
        // 이미 같은 이름이 있으면 생성 중단(중복 카테고리 방지)
        if (questionCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
        }
        QuestionCategory saved = questionCategoryRepository.save(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionCategoryResponse.from(saved));
    }

    // 문제 카테고리 이름 수정
    // @Transactional이 핵심: 조회한 엔티티의 changeName 변경을 트랜잭션 종료 시점에 더티 체킹으로 UPDATE 반영.
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<QuestionCategoryResponse> update(@PathVariable UUID id, @RequestBody @Valid QuestionCategoryRequest request) {
        QuestionCategory category = questionCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        // 같은 이름을 쓰는 다른 카테고리가 있으면 충돌 → 단, 자기 자신(동일 id)은 제외해야
        // "이름 그대로 두고 저장" 같은 케이스가 막히지 않는다.
        questionCategoryRepository.findByName(request.getName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new IllegalArgumentException("이미 사용 중인 카테고리명입니다."); });

        category.changeName(request.getName()); // 영속 상태 엔티티 변경 → save 호출 없이 반영됨
        return ResponseEntity.ok(QuestionCategoryResponse.from(category));
    }

    // 문제 카테고리 제거
    // 존재 확인 없이 삭제 → 없는 id여도 멱등하게 204. 이 카테고리를 참조하는 Question이 있으면 FK 제약에 걸릴 수 있음.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}