package com.main.jobit.domain.study;

// 스터디 진행 방식. 목록 필터(modeEquals)와 화면 배지 표시에 사용된다.
public enum StudyMode {
    ONLINE,    // 온라인 전용 (화상/원격)
    OFFLINE,   // 오프라인 대면
    HYBRID     // 온·오프라인 병행
}