package com.main.jobit.domain.job;

import com.main.jobit.domain.job.JobCategory;
import com.main.jobit.domain.job.JobCategoryRepository;
import com.main.jobit.domain.job.dto.JobCategoryRequest;
import com.main.jobit.domain.job.dto.JobCategoryResponse;
import com.main.jobit.global.security.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// 관리자 전용 직군(JobCategory) 관리 API.
// 클래스 레벨 @Admin AOP로 모든 핸들러가 관리자 권한 검사를 통과해야만 실행된다.
// (단순 CRUD라 별도 Service 없이 컨트롤러에서 레포지토리를 직접 다룬다)
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Admin
public class AdminCategoryController {

    private final JobCategoryRepository categoryRepository;

    // 직군 전체 목록 조회. 직군 수가 많지 않은 마스터 데이터라 페이징 없이 findAll로 일괄 반환.
    @GetMapping
    public ResponseEntity<List<JobCategoryResponse>> listCategories() {
        return ResponseEntity.ok(
                categoryRepository.findAll().stream().map(JobCategoryResponse::from).toList()
        );
    }

    // 직군 생성. 성공 시 201 Created로 저장된 결과를 돌려준다.
    // 주의: 여기서는 중복 name을 앱 단에서 막지 않으므로, 중복 시 DB unique 제약 위반 예외로 떨어진다.
    @PostMapping
    public ResponseEntity<JobCategoryResponse> createCategory(@RequestBody JobCategoryRequest request) {
        JobCategory saved = categoryRepository.save(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(JobCategoryResponse.from(saved));
    }

    // 직군 삭제. 존재 여부를 따로 확인하지 않으므로 없는 id를 지워도 멱등하게 204 처리된다.
    // (이 직군을 참조하는 데이터가 있다면 FK 제약에 걸릴 수 있으니 운영 시 주의)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}