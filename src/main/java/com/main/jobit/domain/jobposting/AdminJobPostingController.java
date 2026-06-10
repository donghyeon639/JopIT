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
    // 진단(diagnose)에서 원문을 직접 보려고 구체 어댑터(ALIO)를 주입. 일반 동기화는 포트(List<Fetcher>)로 충분하지만
    // 이 진단 엔드포인트는 ALIO 응답 원문을 그대로 확인하는 용도라 의도적으로 구현체를 직접 참조한다.
    private final AlioJobPostingFetcher alioJobPostingFetcher;

    /** 즉시 동기화 실행. 60초 initialDelay/6시간 fixedRate 무시하고 바로 돌린다. */
    // 운영 중 새 공고를 곧장 반영하고 싶을 때 사용. 실행 전/후 건수 차이를 반환해 몇 건 추가됐는지 바로 확인할 수 있다.
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        long before = jobPostingRepository.count();
        jobPostingSyncService.syncAll();  // 동기화 후 내부에서 캐시 무효화까지 수행
        long after = jobPostingRepository.count();
        return ResponseEntity.ok(Map.of(
                "alioConfigured", alioJobPostingFetcher.isConfigured(),
                "countBefore", before,
                "countAfter", after,
                "added", after - before
        ));
    }

    /** ALIO 응답 원문 진단. 파싱 전 raw JSON/문자열 그대로 노출 (관리자만). */
    // 동기화 건수가 안 늘 때 외부 API 자체가 무엇을 주는지 확인하는 디버깅 용도. 기본은 소량(3건)만 조회.
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