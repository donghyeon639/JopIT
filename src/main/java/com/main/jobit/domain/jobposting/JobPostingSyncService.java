package com.main.jobit.domain.jobposting;

import com.main.jobit.infra.publicjob.JobPostingFetcher;
import com.main.jobit.infra.publicjob.NormalizedJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 외부 채용 소스 어댑터({@link JobPostingFetcher})를 모두 모아
 * 주기적으로 동기화한다. 새 소스 추가 시 어댑터만 등록하면 자동 포함.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingSyncService {

    private final List<JobPostingFetcher> fetchers;
    private final JobPostingRepository jobPostingRepository;

    private static final int NUM_OF_ROWS = 50;
    private static final int MAX_PAGES = 8;   // IT(R600020) 매칭 비율 ~9% → 8페이지면 약 35건 확보
    private static final long REFRESH_INTERVAL_MS = 6L * 60 * 60 * 1000;  // 6시간
    private static final long INITIAL_DELAY_MS = 10_000L;                  // 10초 (scheduler 백업용)

    /** 앱 기동 직후 한 번 즉시 동기화 — 사용자가 첫 진입에 빈 화면을 보지 않도록. */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void syncOnStartup() {
        log.info("앱 기동 동기화 트리거");
        syncAll();
    }

    @Scheduled(fixedRate = REFRESH_INTERVAL_MS, initialDelay = INITIAL_DELAY_MS)
    @CacheEvict(value = "jobPostings", allEntries = true)
    @Transactional
    public void syncAll() {
        for (JobPostingFetcher fetcher : fetchers) {
            if (!fetcher.isConfigured()) {
                log.info("채용 동기화 skip: source={} (미설정)", fetcher.source());
                continue;
            }
            syncFromFetcher(fetcher);
        }
    }

    private void syncFromFetcher(JobPostingFetcher fetcher) {
        log.info("채용 동기화 시작: source={}", fetcher.source());
        int upserted = 0;
        for (int page = 1; page <= MAX_PAGES; page++) {
            List<NormalizedJob> jobs = fetcher.fetchPage(page, NUM_OF_ROWS);
            if (jobs.isEmpty()) break;
            for (NormalizedJob job : jobs) {
                upsert(fetcher.source(), job);
                upserted++;
            }
            if (jobs.size() < NUM_OF_ROWS) break;
        }
        log.info("채용 동기화 완료: source={}, upserted={}", fetcher.source(), upserted);
    }

    private void upsert(JobSource source, NormalizedJob job) {
        jobPostingRepository.findBySourceAndExternalId(source, job.externalId())
                .ifPresentOrElse(
                        existing -> existing.update(
                                existing.getJobCategory(),
                                job.title(), job.company(), job.location(),
                                job.careerLevel(), job.employmentType(), job.salaryRange(),
                                job.postedAt(), job.expiresAt(), job.applyUrl(), null
                        ),
                        () -> jobPostingRepository.save(JobPosting.builder()
                                .source(source)
                                .externalId(job.externalId())
                                .title(job.title())
                                .company(job.company())
                                .location(job.location())
                                .careerLevel(job.careerLevel())
                                .employmentType(job.employmentType())
                                .salaryRange(job.salaryRange())
                                .postedAt(job.postedAt())
                                .expiresAt(job.expiresAt())
                                .applyUrl(job.applyUrl())
                                .build())
                );
    }
}