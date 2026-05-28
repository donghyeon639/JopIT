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

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Admin
public class AdminCategoryController {

    private final JobCategoryRepository categoryRepository;

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
}