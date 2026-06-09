package com.main.jobit.domain.study;

/**
 * 작성자가 신청에 대해 취할 수 있는 액션.
 * 상태(JoinRequestStatus)와 분리한 이유: API 요청 본문은 "무엇을 할지(동사)"를 받고,
 * 그 결과로 엔티티의 상태(명사)가 ACCEPTED/REJECTED로 바뀐다. 요청 DTO와 영속 상태를 결합하지 않기 위함.
 */
public enum JoinRequestAction {
    ACCEPT,   // 신청 수락 → status = ACCEPTED
    REJECT    // 신청 거절 → status = REJECTED
}
