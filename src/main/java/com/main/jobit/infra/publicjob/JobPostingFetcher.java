package com.main.jobit.infra.publicjob;

import com.main.jobit.domain.jobposting.JobSource;

import java.util.List;

/**
 * 외부 채용 정보 소스 어댑터 포트.
 * 새 소스(사람인, 워크넷, 원티드 등)를 붙일 때 이 인터페이스를 구현하는
 * Spring 컴포넌트를 하나 추가하면 {@code JobPostingSyncService}가 자동으로 동기화한다.
 */
public interface JobPostingFetcher {

    JobSource source();

    boolean isConfigured();

    List<NormalizedJob> fetchPage(int pageNo, int numOfRows);
}