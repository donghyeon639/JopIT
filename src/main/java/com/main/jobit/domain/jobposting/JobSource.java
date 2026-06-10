package com.main.jobit.domain.jobposting;

// 채용 공고의 출처(외부 데이터 소스) 식별 enum.
// JobPosting은 (source, externalId) 조합으로 유일성을 보장하므로, 같은 공고라도 소스가 다르면 별도 행으로 적재된다.
// 동기화 어댑터(JobPostingFetcher.source())가 반환하는 문자열이 이 enum 이름과 일치해야 valueOf로 매핑된다.
// 현재 실제 연동은 공공데이터(ALIO) 위주이며, 나머지는 향후 소스 확장을 위한 자리값.
public enum JobSource {
    SARAMIN,     // 사람인 OpenAPI (향후 연동)
    WORKNET,     // 워크넷 (향후 연동)
    WANTED,      // 원티드 (향후 연동)
    PUBLIC_DATA  // 공공데이터포털(ALIO 등 공공기관 채용)
}