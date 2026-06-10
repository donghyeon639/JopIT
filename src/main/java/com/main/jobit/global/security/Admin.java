package com.main.jobit.global.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
  ROLE_ADMIN 권한을 가진 사용자만 접근할 수 있음을 나타내는 마커 어노테이션.
  클래스(컨트롤러 전체) 또는 개별 메서드에 부착
  실제 권한 검증은 {@link AdminAspect}가 수행
  /api/admin/** URL 규칙(SecurityConfig)과 함께 다층 방어로 동작
 */

// @Target: 타입(컨트롤러 전체)과 메서드 양쪽에 부착 가능.
// @Retention(RUNTIME): AOP가 런타임에 어노테이션 존재를 읽어 권한 검증해야 하므로 런타임까지 유지.
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Admin {
}