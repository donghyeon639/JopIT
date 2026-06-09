package com.main.jobit.ai.port;

/**
 * 도메인이 의존하는 LLM 추상화. 도메인 코드는 구체 어댑터(ClaudeCliService 등)를 직접 참조하지 않고
 * 이 포트만 본다. AI 어댑터 교체(CLI -> SDK, Claude -> 다른 모델)는 이 인터페이스 구현체만 바꾸면 끝.
 */
public interface LlmPort {

    // 프롬프트 문자열을 받아 LLM이 생성한 응답 텍스트를 반환한다.
    // 동기 호출 가정(현재 어댑터인 ClaudeCliService는 스트리밍 미지원). 입출력 계약을 단순 String<->String으로
    // 고정해 두면 모델/구현이 바뀌어도 도메인 코드는 영향을 받지 않는다.
    String generate(String prompt);
}