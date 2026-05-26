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

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    private static final int MAX_PAGE_SIZE = 50;

    @Cacheable(
            value = "jobPostings",
            key = "(#source == null ? 'all' : #source.name()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public QuestionPagedResponse<JobPostingResponse> list(JobSource source, Pageable pageable) {
        Pageable safe = sanitize(pageable);

        Page<JobPosting> page = (source == null)
                ? jobPostingRepository.findByActiveTrue(safe)
                : jobPostingRepository.findBySourceAndActiveTrue(source, safe);

        return QuestionPagedResponse.from(page.map(JobPostingResponse::from));
    }

    private Pageable sanitize(Pageable raw) {
        int size = Math.clamp(raw.getPageSize(), 1, MAX_PAGE_SIZE);
        Sort sort = raw.getSort().isSorted()
                ? raw.getSort()
                : Sort.by(Sort.Order.desc("postedAt"));
        return PageRequest.of(raw.getPageNumber(), size, sort);
    }
}