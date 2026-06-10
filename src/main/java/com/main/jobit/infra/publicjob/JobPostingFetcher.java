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

    // 이 어댑터가 대표하는 소스 식별 문자열(도메인 enum 이름과 동일하게 맞춘다).
    String source();

    // 호출에 필요한 설정(예: API 키)이 갖춰졌는지. false면 SyncService가 호출을 건너뛴다.
    boolean isConfigured();

    // 페이지 단위로 채용 데이터를 가져와 NormalizedJob 목록으로 변환해 반환한다.
    // 페이징·정규화·소스별 예외 처리는 각 어댑터 책임. 실패 시 빈 목록 반환(예외를 밖으로 던지지 않는 게 컨벤션).
    List<NormalizedJob> fetchPage(int pageNo, int numOfRows);
}