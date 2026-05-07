package com.main.jobit.resume;

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

@Slf4j
@Component
public class ResumeTextExtractor {

    private static final int MAX_CHARS = 200_000;

    private final Tika tika = new Tika();

    public Extracted extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String mimeType;
        try (InputStream in = file.getInputStream()) {
            mimeType = tika.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다.");
        }

        try (InputStream in = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(MAX_CHARS);
            Metadata metadata = new Metadata();
            new AutoDetectParser().parse(in, handler, metadata, new ParseContext());

            String text = handler.toString().trim();
            if (text.isEmpty()) {
                throw new IllegalArgumentException(
                        "문서에서 텍스트를 추출하지 못했습니다. 스캔된 이미지 PDF는 지원하지 않습니다.");
            }
            return new Extracted(text, mimeType);
        } catch (IOException | SAXException | TikaException e) {
            log.error("문서 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "지원하지 않거나 손상된 문서입니다. PDF, DOCX, DOC, RTF, ODT, TXT를 지원합니다.");
        }
    }

    public record Extracted(String text, String mimeType) {}
}