package com.main.jobit.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 회원 등록 요청 DTO(email/직군 ID 기반 형태).
// 현재 주력 가입 경로는 SignupRequest(username 기반)이며, 이 DTO는 email·jobCategoryId 기반 등록을 위한 별도 표현이다.
// 두 형태가 공존하므로 가입 흐름을 손볼 때 어느 DTO를 쓰는 경로인지 확인이 필요하다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    private String email;
    private String password;
    private String nickname;
    private UUID jobCategoryId;
}