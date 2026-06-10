package com.main.jobit.domain.resume.dto;

// 이력서 AI 피드백 API 응답 DTO.
// feedback           : LLM이 생성한 마크다운 첨삭 본문(섹션 1~5 구조)
// extractedCharCount : 실제로 LLM에 전달된(추출/정제된) 글자 수 — 클라이언트의 정보성 표시용
// detectedFileType   : 입력 형식. 파일이면 Tika가 감지한 MIME, 텍스트 입력이면 "text/plain"
public record ResumeFeedbackResponse(
        String feedback,
        int extractedCharCount,
        String detectedFileType
) {}