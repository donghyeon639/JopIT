package com.main.jobit.domain.resume;

import com.main.jobit.domain.resume.dto.ResumeFeedbackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// 이력서 AI 피드백 진입점(REST). 클라이언트는 파일 업로드 또는 텍스트 붙여넣기 중
// 한 가지 방식으로 이력서를 보내고, 동기 응답으로 LLM 첨삭 결과를 받는다.
// 실제 추출·프롬프트·LLM 호출은 ResumeFeedbackService에 위임(컨트롤러는 입력 분기만 담당).
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeFeedbackService resumeFeedbackService;

    // multipart/form-data로 받는 이유: 파일(MultipartFile)과 폼 필드(text, jobCategory)를 한 요청에 함께 받기 위함.
    // file / text는 모두 required=false로 두고, 정확히 하나만 들어왔는지 메서드 내부에서 검증한다.
    @PostMapping(value = "/feedback", consumes = "multipart/form-data")
    public ResponseEntity<ResumeFeedbackResponse> feedback(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "jobCategory", required = false) String jobCategory) {

        // 파일이 실제로 첨부되었는지(빈 파트가 아닌지), 텍스트가 공백만은 아닌지 각각 판별.
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasText = text != null && !text.isBlank();

        // hasFile == hasText 가 true인 경우 = 둘 다 있거나(true==true) 둘 다 없는(false==false) 상황.
        // 입력 방식은 반드시 둘 중 "정확히 하나"여야 하므로 그 외에는 잘못된 요청으로 거른다.
        if (hasFile == hasText) {
            throw new IllegalArgumentException("파일 또는 텍스트 중 하나만 입력해주세요.");
        }

        // 파일이면 Tika 추출 경로(fromFile), 텍스트면 바로 LLM 경로(fromText)로 분기.
        ResumeFeedbackResponse response = hasFile
                ? resumeFeedbackService.fromFile(file, jobCategory)
                : resumeFeedbackService.fromText(text, jobCategory);

        return ResponseEntity.ok(response);
    }
}