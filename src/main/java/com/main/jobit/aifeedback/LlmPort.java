package com.main.jobit.aifeedback;

/**
 * 도메인이 의존하는 LLM 추상화. 도메인 코드는 구체 어댑터(ClaudeCliService 등)를 직접 참조하지 않고
 * 이 포트만 본다. AI 어댑터 교체(CLI -> SDK, Claude -> 다른 모델)는 이 인터페이스 구현체만 바꾸면 끝.
 */
public interface LlmPort {

    String generate(String prompt);
}