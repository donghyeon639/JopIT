package com.main.jobit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JobIT 백엔드의 진입점(엔트리 포인트).
// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan 조합.
// 이 클래스가 위치한 com.main.jobit 패키지를 기준으로 하위 모든 컴포넌트를 스캔한다.
@SpringBootApplication
public class JobitApplication {

    // 애플리케이션 부팅: 내장 톰캣 기동 + 스프링 컨텍스트 초기화.
    public static void main(String[] args) {
        SpringApplication.run(JobitApplication.class, args);
    }

}
