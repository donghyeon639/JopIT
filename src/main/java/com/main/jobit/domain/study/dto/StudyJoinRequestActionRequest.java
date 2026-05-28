package com.main.jobit.domain.study.dto;

import com.main.jobit.domain.study.JoinRequestAction;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyJoinRequestActionRequest {

    @NotNull(message = "처리할 액션(ACCEPT/REJECT)을 지정해주세요.")
    private JoinRequestAction action;
}
