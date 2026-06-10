package com.main.jobit.domain.jobposting;

import com.main.jobit.domain.jobposting.dto.JobPostingResponse;
import com.main.jobit.domain.question.dto.QuestionPagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 채용 공고 공개 조회 서비스. 동기화는 JobPostingSyncService가 맡고, 여기서는 읽기 전용 페이징 조회만 한다.
@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    private static final int MAX_PAGE_SIZE = 50;  // 과도한 페이지 크기로 인한 부하 방지 상한

    // 목록 조회 결과를 Caffeine 캐시("jobPostings")에 보관.
    // 캐시 키는 (소스 + 페이지번호 + 페이지크기) 조합 — 소스 미지정 시 'all'. 이렇게 해야 필터/페이지별로 캐시가 분리된다.
    // 동기화(JobPostingSyncService.syncAll)가 끝나면 @CacheEvict로 이 캐시를 통째로 비워 최신 공고가 반영된다.
    @Cacheable(
            value = "jobPostings",
            key = "(#source == null ? 'all' : #source.name()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public QuestionPagedResponse<JobPostingResponse> list(JobSource source, Pageable pageable) {
        Pageable safe = sanitize(pageable);  // 클라이언트가 보낸 페이징 파라미터를 안전 범위로 보정

        // 소스 미지정이면 전체, 지정이면 해당 소스만. 둘 다 active=true 공고만 노출.
        Page<JobPosting> page = (source == null)
                ? jobPostingRepository.findByActiveTrue(safe)
                : jobPostingRepository.findBySourceAndActiveTrue(source, safe);

        return QuestionPagedResponse.from(page.map(JobPostingResponse::from));
    }

    // 페이징 파라미터 정규화: 페이지 크기를 1~50으로 clamp하고, 정렬이 없으면 게시일 내림차순(최신순)을 기본 적용.
    private Pageable sanitize(Pageable raw) {
        int size = Math.clamp(raw.getPageSize(), 1, MAX_PAGE_SIZE);
        Sort sort = raw.getSort().isSorted()
                ? raw.getSort()
                : Sort.by(Sort.Order.desc("postedAt"));
        return PageRequest.of(raw.getPageNumber(), size, sort);
    }
}