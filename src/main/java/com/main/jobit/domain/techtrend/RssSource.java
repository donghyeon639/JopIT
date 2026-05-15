package com.main.jobit.domain.techtrend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RssSource {
    KAKAO    ("카카오 기술 블로그",       "https://tech.kakao.com/feed/"),
    WOOWAHAN ("우아한형제들 기술 블로그", "https://techblog.woowahan.com/feed/"),
    NAVER_D2 ("네이버 D2",              "https://d2.naver.com/d2.atom"),
    LINE     ("LINE 기술 블로그",        "https://engineering.linecorp.com/ko/feed/");

    private final String name;
    private final String url;
}