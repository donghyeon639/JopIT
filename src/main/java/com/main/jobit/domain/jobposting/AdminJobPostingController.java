package com.main.jobit.domain.jobposting;

import com.main.jobit.global.security.Admin;
import com.main.jobit.infra.publicjob.alio.AlioJobPostingFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 동기화 트리거 + 외부 API 응답 진단용 관리자 엔드포인트.
 * /api/admin/** 는 SecurityConfig에서 ROLE_ADMIN으로 보호되어 있다.
 */
@RestController
@RequestMapping("/api/admin/job-postings")
@RequiredArgsConstructor
@Admin
public class AdminJobPostingController {

    private final JobPostingSyncService jobPostingSyncService;
    private final JobPostingRepository jobPostingRepository;
    private final AlioJobPostingFetcher alioJobPostingFetcher;

    /** 즉시 동기화 실행. 60초 initialDelay/6시간 fixedRate 무시하고 바로 돌린다. */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        long before = jobPostingRepository.count();
        jobPostingSyncService.syncAll();
        long after = jobPostingRepository.count();
        return ResponseEntity.ok(Map.of(
                "alioConfigured", alioJobPostingFetcher.isConfigured(),
                "countBefore", before,
                "countAfter", after,
                "added", after - before
        ));
    }

    /** ALIO 응답 원문 진단. 파싱 전 raw JSON/문자열 그대로 노출 (관리자만). */
    @GetMapping("/diagnose")
    public ResponseEntity<Map<String, Object>> diagnose(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "3") int numOfRows) {
        String raw = alioJobPostingFetcher.fetchPageRaw(pageNo, numOfRows);
        return ResponseEntity.ok(Map.of(
                "alioConfigured", alioJobPostingFetcher.isConfigured(),
                "pageNo", pageNo,
                "numOfRows", numOfRows,
                "responseLength", raw == null ? 0 : raw.length(),
                "responseBody", raw == null ? "" : raw
        ));
    }
}