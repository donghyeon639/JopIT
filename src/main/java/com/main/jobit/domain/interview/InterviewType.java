package com.main.jobit.domain.interview;

/**
 * 면접 종류. 사용자가 면접 시작 전에 선택한다.
 * label은 LLM 프롬프트·화면 표기에 사용.
 */
public enum InterviewType {

    PERSONALITY("인성 면접"),
    IN_DEPTH("심층 면접");

    private final String label;

    InterviewType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
