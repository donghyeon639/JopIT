package com.main.jobit.domain.study.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 참여 신청 요청 DTO. body가 통째로 생략될 수 있어(컨트롤러에서 null이면 빈 객체로 대체) 모든 필드가 선택 입력이다.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyApplyRequest {

    /** 자기소개 / 신청 한 마디. 선택 입력. */
    @Size(max = 1000, message = "신청 메시지는 1,000자 이내로 입력해주세요.")
    private String message;
}
