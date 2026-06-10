package com.main.jobit.domain.study.dto;

import com.main.jobit.domain.study.JoinRequestAction;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 신청 수락/거절 요청 DTO. 작성자가 PATCH로 보내는 처리 명령을 담는다.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyJoinRequestActionRequest {

    // 수행할 액션. 누락 시 어떤 처리인지 알 수 없으므로 필수.
    @NotNull(message = "처리할 액션(ACCEPT/REJECT)을 지정해주세요.")
    private JoinRequestAction action;
}
