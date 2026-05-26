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

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    public ResponseEntity<QuestionPagedResponse<JobPostingResponse>> list(
            @RequestParam(required = false) JobSource source,
            @PageableDefault(size = 9, sort = "postedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(jobPostingService.list(source, pageable));
    }
}