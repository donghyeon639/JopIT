package com.main.jobit.domain.techtrend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 수집 대상 기술 블로그 RSS/Atom 피드 목록을 enum 상수로 고정.
// 소스를 코드 상수로 관리하므로 DB 설정 없이 배포만으로 소스를 추가/제거할 수 있다.
// TechTrendService.refreshArticles()가 RssSource.values()를 순회하며 동기화하므로,
// 새 블로그를 추가하려면 여기에 상수 한 줄만 추가하면 자동으로 수집 대상에 포함된다.
@Getter
@RequiredArgsConstructor
public enum RssSource {
    // (표시용 이름, 피드 URL) — name은 TechArticle.source에 그대로 저장된다.
    // 네이버 D2만 Atom 포맷(.atom)이고 나머지는 RSS이지만, ROME(SyndFeed)이 양쪽을 동일하게 파싱한다.
    KAKAO    ("카카오 기술 블로그",       "https://tech.kakao.com/feed/"),
    WOOWAHAN ("우아한형제들 기술 블로그", "https://techblog.woowahan.com/feed/"),
    NAVER_D2 ("네이버 D2",              "https://d2.naver.com/d2.atom"),
    LINE     ("LINE 기술 블로그",        "https://engineering.linecorp.com/ko/feed/");

    private final String name;  // 화면/저장에 노출되는 출처 이름
    private final String url;   // ROME이 읽어올 피드 주소
}