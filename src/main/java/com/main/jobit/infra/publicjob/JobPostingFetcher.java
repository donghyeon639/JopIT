package com.main.jobit.infra.publicjob;

import java.util.List;

/**
 * 외부 채용 정보 소스 어댑터 포트.
 * 새 소스(사람인, 워크넷, 원티드 등)를 붙일 때 이 인터페이스를 구현하는
 * Spring 컴포넌트를 하나 추가하면 {@code JobPostingSyncService}가 자동으로 동기화한다.
 *
 * source()는 도메인 enum 이름과 동일한 문자열을 반환한다 (예: "PUBLIC_DATA").
 * 도메인 enum을 직접 노출하지 않는 이유는 infra 모듈이 도메인을 모르도록 분리하기 위함.
 */
public interface JobPostingFetcher {

    String source();

    boolean isConfigured();

    List<NormalizedJob> fetchPage(int pageNo, int numOfRows);
}