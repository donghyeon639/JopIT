package com.main.jobit.domain.resume;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

// 업로드된 이력서 파일을 Apache Tika로 평문 텍스트 + MIME 타입으로 변환하는 어댑터.
// PDF/DOCX/DOC/RTF/ODT/TXT 등 다양한 포맷을 AutoDetectParser가 형식별로 자동 파싱한다.
// 추출 단계에서 발생하는 모든 저수준 예외(IO/SAX/Tika)는 사용자 친화적 IllegalArgumentException으로 변환해 던진다.
@Slf4j
@Component
public class ResumeTextExtractor {

    // 추출 텍스트 상한. BodyContentHandler 기본값은 10만 자에서 끊기므로(WriteLimitReachedException 발생),
    // 넉넉히 20만 자로 올려 정상 이력서가 중간에 잘리지 않게 한다. (LLM 입력 제한은 서비스 계층에서 별도 적용)
    private static final int MAX_CHARS = 200_000;

    // Tika 인스턴스는 thread-safe하므로 컴포넌트 싱글턴 필드로 재사용한다.
    private final Tika tika = new Tika();

    // 파일 → (텍스트, MIME) 추출. 입력 스트림은 일회성이라 detect와 parse에서 각각 새로 연다.
    public Extracted extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 1차: MIME 타입 감지. 확장자(originalFilename)와 내용 매직 바이트를 함께 활용해 정확도를 높인다.
        String mimeType;
        try (InputStream in = file.getInputStream()) {
            mimeType = tika.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다.");
        }

        // 2차: 본문 텍스트 추출. detect에서 쓴 스트림은 이미 소비됐으므로 새 스트림을 연다.
        try (InputStream in = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(MAX_CHARS);  // 추출 길이 한도 지정
            Metadata metadata = new Metadata();
            // AutoDetectParser가 포맷을 스스로 판별해 적절한 파서로 위임한다.
            new AutoDetectParser().parse(in, handler, metadata, new ParseContext());

            String text = handler.toString().trim();
            // 텍스트가 비었다면 대표적으로 스캔 이미지 PDF(텍스트 레이어 없음) — OCR 미지원이므로 명시적으로 안내.
            if (text.isEmpty()) {
                throw new IllegalArgumentException(
                        "문서에서 텍스트를 추출하지 못했습니다. 스캔된 이미지 PDF는 지원하지 않습니다.");
            }
            return new Extracted(text, mimeType);
        } catch (IOException | SAXException | TikaException e) {
            // 저수준 파싱 예외는 로그로만 원인을 남기고, 사용자에게는 지원 포맷을 안내하는 일반 메시지로 변환.
            log.error("문서 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "지원하지 않거나 손상된 문서입니다. PDF, DOCX, DOC, RTF, ODT, TXT를 지원합니다.");
        }
    }

    // 추출 결과 묶음(추출 텍스트 + 감지된 MIME 타입). 호출부로 두 값을 함께 안전하게 전달한다.
    public record Extracted(String text, String mimeType) {}
}