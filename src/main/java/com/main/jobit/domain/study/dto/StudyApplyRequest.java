package com.main.jobit.domain.study.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyApplyRequest {

    /** 자기소개 / 신청 한 마디. 선택 입력. */
    @Size(max = 1000, message = "신청 메시지는 1,000자 이내로 입력해주세요.")
    private String message;
}
