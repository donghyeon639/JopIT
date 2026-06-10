package com.main.jobit.domain.user;

// 회원 권한 등급. JWT 클레임에 문자열(name())로 실려 인가 판단에 사용된다.
// USER: 일반 사용자(기본값), ADMIN: 관리자 기능(@Admin AOP로 보호되는 엔드포인트) 접근 가능.
// 값 추가/이름 변경 시 토큰 발급부와 SecurityConfig 인가 규칙을 함께 점검해야 한다.
public enum Role {
    USER, ADMIN
}
