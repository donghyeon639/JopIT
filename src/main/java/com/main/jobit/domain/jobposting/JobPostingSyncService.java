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

    // 등록된 모든 채용 소스 어댑터를 스프링이 List로 주입. 새 어댑터(@Component)를 추가하면 자동으로 여기 합류한다.
    // → 포트(JobPostingFetcher)-어댑터 경계 덕에 동기화 로직을 건드리지 않고 소스만 늘릴 수 있다.
    private final List<JobPostingFetcher> fetchers;
    private final JobPostingRepository jobPostingRepository;

    private static final int NUM_OF_ROWS = 50;  // 한 페이지에 요청할 건수
    private static final int MAX_PAGES = 8;   // IT(R600020) 매칭 비율 ~9% → 8페이지면 약 35건 확보
    private static final long REFRESH_INTERVAL_MS = 6L * 60 * 60 * 1000;  // 6시간. 채용은 자주 갱신되므로 RSS(3일)보다 짧게
    private static final long INITIAL_DELAY_MS = 10_000L;                  // 10초 (scheduler 백업용)

    /** 앱 기동 직후 한 번 즉시 동기화 — 사용자가 첫 진입에 빈 화면을 보지 않도록. */
    // @Async라 기동 스레드를 막지 않고 백그라운드에서 수행. 스케줄러 첫 실행(10초 후)과 별개의 즉시 트리거다.
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void syncOnStartup() {
        log.info("앱 기동 동기화 트리거");
        syncAll();
    }

    // 6시간 주기 전체 동기화 진입점(관리자 수동 트리거에서도 호출).
    // 미설정 어댑터(API 키 없음 등)는 건너뛰고, 끝나면 목록 캐시("jobPostings")를 통째로 비워 최신 공고를 노출한다.
    // 전체가 한 트랜잭션이라 upsert의 변경이 종료 시점에 일괄 flush된다.
    @Scheduled(fixedRate = REFRESH_INTERVAL_MS, initialDelay = INITIAL_DELAY_MS)
    @CacheEvict(value = "jobPostings", allEntries = true)
    @Transactional
    public void syncAll() {
        for (JobPostingFetcher fetcher : fetchers) {
            // 시크릿/설정이 없는 소스는 조용히 skip(부분 가용성 허용).
            if (!fetcher.isConfigured()) {
                log.info("채용 동기화 skip: source={} (미설정)", fetcher.source());
                continue;
            }
            syncFromFetcher(fetcher);
        }
    }

    // 단일 어댑터에서 페이지를 넘기며 공고를 받아 upsert.
    // 어댑터가 돌려준 source 문자열을 JobSource enum으로 매핑하지 못하면 해당 소스만 skip(나머지는 진행).
    private void syncFromFetcher(JobPostingFetcher fetcher) {
        JobSource source;
        try {
            source = JobSource.valueOf(fetcher.source());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 채용 소스 식별자: {}. 동기화 skip.", fetcher.source());
            return;
        }
        log.info("채용 동기화 시작: source={}", source);
        int upserted = 0;
        // 페이지네이션: 최대 MAX_PAGES까지. 빈 페이지면 종료, 마지막 페이지(요청 건수 미만)면 더 안 돌고 종료.
        for (int page = 1; page <= MAX_PAGES; page++) {
            List<NormalizedJob> jobs = fetcher.fetchPage(page, NUM_OF_ROWS);
            if (jobs.isEmpty()) break;
            for (NormalizedJob job : jobs) {
                upsert(source, job);
                upserted++;
            }
            if (jobs.size() < NUM_OF_ROWS) break;  // 마지막 페이지로 판단 → 추가 호출 불필요
        }
        log.info("채용 동기화 완료: source={}, upserted={}", source, upserted);
    }

    // (source, externalId) 기준 upsert: 있으면 변동 필드만 갱신(active 되살림), 없으면 새로 저장.
    // 기존 행 갱신 시 jobCategory는 기존 분류 유지, rawPayload는 null로 넘겨 굳이 덮어쓰지 않는다.
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