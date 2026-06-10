package com.main.jobit.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// 비동기 처리 활성화 설정.
// @EnableAsync가 있어야 @Async 메서드(예: AiFeedbackService.requestFeedback)가 실제로 별도 스레드에서 실행된다.
// 이 설정이 없으면 @Async는 무시되고 호출 스레드에서 동기 실행되어 API 응답이 LLM 응답까지 블로킹된다.
// 별도 Executor 빈을 두지 않았으므로 스프링 기본 SimpleAsyncTaskExecutor를 사용 — 부하가 커지면 풀 설정 추가 검토.
@Configuration
@EnableAsync
public class AsyncConfig {
}