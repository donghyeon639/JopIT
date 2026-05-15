package com.main.jobit.resume;

import com.main.jobit.resume.dto.ResumeFeedbackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeFeedbackService resumeFeedbackService;

    @PostMapping(value = "/feedback", consumes = "multipart/form-data")
    public ResponseEntity<ResumeFeedbackResponse> feedback(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "jobCategory", required = false) String jobCategory) {

        boolean hasFile = file != null && !file.isEmpty();
        boolean hasText = text != null && !text.isBlank();

        if (hasFile == hasText) {
            throw new IllegalArgumentException("파일 또는 텍스트 중 하나만 입력해주세요.");
        }

        ResumeFeedbackResponse response = hasFile
                ? resumeFeedbackService.fromFile(file, jobCategory)
                : resumeFeedbackService.fromText(text, jobCategory);

        return ResponseEntity.ok(response);
    }
}