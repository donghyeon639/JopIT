package com.main.prephub.admin;

import com.main.prephub.job.JobCategory;
import com.main.prephub.job.JobCategoryRepository;
import com.main.prephub.job.JobDetail;
import com.main.prephub.job.JobDetailRepository;
import com.main.prephub.job.dto.JobCategoryRequest;
import com.main.prephub.job.dto.JobCategoryResponse;
import com.main.prephub.job.dto.JobDetailRequest;
import com.main.prephub.job.dto.JobDetailResponse;
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

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final JobCategoryRepository categoryRepository;
    private final JobDetailRepository detailRepository;

    @GetMapping
    public ResponseEntity<List<JobCategoryResponse>> listCategories() {
        return ResponseEntity.ok(
                categoryRepository.findAll().stream().map(JobCategoryResponse::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<JobCategoryResponse> createCategory(@RequestBody JobCategoryRequest request) {
        JobCategory saved = categoryRepository.save(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(JobCategoryResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/details")
    public ResponseEntity<List<JobDetailResponse>> listDetails(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(
                detailRepository.findByCategoryId(categoryId).stream()
                        .map(JobDetailResponse::from).toList()
        );
    }

    @PostMapping("/{categoryId}/details")
    public ResponseEntity<JobDetailResponse> createDetail(
            @PathVariable UUID categoryId,
            @RequestBody JobDetailRequest request) {
        JobCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        JobDetail saved = detailRepository.save(request.toEntity(category));
        return ResponseEntity.status(HttpStatus.CREATED).body(JobDetailResponse.from(saved));
    }

    @DeleteMapping("/details/{id}")
    public ResponseEntity<Void> deleteDetail(@PathVariable UUID id) {
        detailRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}