package com.main.jobit.domain.interview.dto;

import java.util.UUID;

// 세션 상세 응답에 담기는 질문 1건 표현. 엔티티를 그대로 노출하지 않고 화면에 필요한 필드만 추린 읽기 전용 DTO.
public record InterviewQuestionResponse(
        UUID id,            // 답변 제출 시 path 변수로 다시 보내는 질문 식별자
        int orderNo,        // 표시 순서(1번=자기소개)
        String content,     // 질문 본문
        String status,      // QuestionStatus.name() 문자열. 프런트가 답변/평가 진행도를 분기하는 데 사용
        String transcript,  // 제출된 답변 텍스트. 미답변이면 null
        String evaluation   // LLM 평가 결과(마크다운). 평가 완료 전이면 null
) {}
