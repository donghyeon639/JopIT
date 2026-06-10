package com.main.jobit.domain.study;

// 모집글의 생명주기 상태. 생성 시 RECRUITING으로 시작하며 작성자가 close()로 CLOSED 전환한다.
// CLOSED가 되면 수정·신청이 막힌다(Service에서 검증). 마감 전용 상태만 있고 "삭제" 상태는 별도로 두지 않음.
public enum StudyStatus {
    RECRUITING,   // 모집 중 (기본값)
    CLOSED        // 모집 마감
}