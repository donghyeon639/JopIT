package com.main.jobit.domain.study;

// 참여 신청의 상태 머신. 신청 시 PENDING으로 시작하며 작성자가 단방향으로 ACCEPTED/REJECTED로 확정한다.
// 한 번 처리(ACCEPTED/REJECTED)되면 다시 PENDING으로 돌아가지 않는다(decide()에서 isPending() 가드).
// 정원 카운트는 ACCEPTED 수만 집계한다(PENDING은 정원에 포함되지 않음).
public enum JoinRequestStatus {
    PENDING,    // 신청 접수, 작성자 처리 대기
    ACCEPTED,   // 작성자 수락 (정원에 카운트됨)
    REJECTED    // 작성자 거절
}
