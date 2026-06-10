package com.main.jobit.domain.interview;

/**
 * 면접 종류. 사용자가 면접 시작 전에 선택한다.
 * label은 LLM 프롬프트·화면 표기에 사용.
 */
public enum InterviewType {

    // enum 상수명(PERSONALITY/IN_DEPTH)은 API·DB 저장값으로 쓰고,
    // 괄호 안 한글 label은 화면 표기와 LLM 프롬프트("~ 면접관입니다") 문구에 들어간다.
    PERSONALITY("인성 면접"),
    IN_DEPTH("심층 면접");

    // 사람이 읽는 한글 이름. 식별자(name())과 분리해 표기 문구를 자유롭게 바꿀 수 있게 한다.
    private final String label;

    InterviewType(String label) {
        this.label = label;
    }

    // 화면/프롬프트용 한글 라벨 반환. enum 표준 name()과 구분하기 위해 별도 접근자로 노출.
    public String label() {
        return label;
    }
}
