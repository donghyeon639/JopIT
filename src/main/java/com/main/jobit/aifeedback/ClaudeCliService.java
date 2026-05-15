package com.main.jobit.aifeedback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaudeCliService implements LlmPort {

    @Value("${claude.cli.timeout-seconds:120}")
    private int timeoutSeconds;

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    @Override
    public String generate(String prompt) {
        return call(prompt);
    }

    public String call(String prompt) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("claude-prompt-", ".txt");
            Files.writeString(tempFile, prompt, StandardCharsets.UTF_8);

            String npxBin = IS_WINDOWS ? "npx.cmd" : "npx";
            ProcessBuilder pb = new ProcessBuilder(
                    npxBin, "-y", "@anthropic-ai/claude-code",
                    "--print",
                    "--output-format", "text",
                    "--dangerously-skip-permissions"
            );
            pb.redirectInput(tempFile.toFile());
            pb.redirectErrorStream(false);

            log.info("Claude CLI 실행 시작");
            Process process = pb.start();

            String stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            String stderr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Claude CLI 응답 시간 초과 (" + timeoutSeconds + "초)");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Claude CLI 오류 (exitCode={}): {}", exitCode, stderr);
                throw new RuntimeException("Claude CLI 실행 실패 (exitCode=" + exitCode + ")");
            }

            log.info("Claude CLI 실행 완료");
            return stdout.trim();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Claude CLI 실행 중 인터럽트 발생", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Claude CLI 호출 실패", e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }
}