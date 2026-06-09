package com.main.jobit.domain.jobposting;

import com.main.jobit.domain.jobposting.dto.JobPostingResponse;
import com.main.jobit.domain.question.dto.QuestionPagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 채용 공고 공개 조회 API. 동기화/관리는 AdminJobPostingController가 따로 담당한다.
@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    // 공고 목록 조회. source는 선택(미지정 시 전체), 기본 9개씩 게시일 내림차순(최신순) 페이징.
    // (페이지 크기 상한 등 추가 보정은 서비스의 sanitize에서 수행.)
    @GetMapping
    public ResponseEntity<QuestionPagedResponse<JobPostingResponse>> list(
            @RequestParam(required = false) JobSource source,
            @PageableDefault(size = 9, sort = "postedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(jobPostingService.list(source, pageable));
    }
}