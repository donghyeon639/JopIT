package com.main.jobit.ai.claude;

import com.main.jobit.ai.port.LlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// LlmPort의 현재 구현 어댑터. `npx @anthropic-ai/claude-code` CLI를 자식 프로세스로 띄워 프롬프트를 처리한다.
// 콜드 스타트(5~10초)·스트리밍 불가 한계가 있어, 화상 면접 등 실시간 기능 착수 시 SDK/로컬 LLM 어댑터로 교체 예정.
// 그때도 LlmPort만 구현하면 되도록 도메인은 이 클래스를 직접 참조하지 않는다.
@Slf4j
@Service
public class ClaudeCliService implements LlmPort {

    // CLI 응답 대기 제한 시간(초). 외부 설정으로 조정 가능하며 미설정 시 120초.
    @Value("${claude.cli.timeout-seconds:120}")
    private int timeoutSeconds;

    // OS별 실행 바이너리 분기용(npx vs npx.cmd). JVM 시작 시 1회만 판정.
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    /** 시간 초과로 강제 종료한 자식 프로세스의 stderr를 끝까지 비우는 데 더 기다릴 최대 시간. */
    private static final int STREAM_DRAIN_SECONDS = 5;

    // LlmPort 계약 구현 — 실제 처리는 call()에 위임.
    @Override
    public String generate(String prompt) {
        return call(prompt);
    }

    // CLI를 실제로 실행하고 stdout(생성 텍스트)을 반환한다. 타임아웃/비정상 종료는 RuntimeException으로 변환.
    public String call(String prompt) {
        long startNanos = System.nanoTime();   // 경과 시간 측정 기준점(로깅용).
        Path tempFile = null;
        Process process = null;
        try {
            // 프롬프트를 임시 파일에 쓰고 stdin으로 리다이렉트한다.
            // 커맨드라인 인자로 넘기면 길이 제한·이스케이프 문제가 생기므로 파일 경유가 안전.
            tempFile = Files.createTempFile("claude-prompt-", ".txt");
            Files.writeString(tempFile, prompt, StandardCharsets.UTF_8);

            // --print: 대화형 UI 없이 1회 응답 후 종료. --output-format text: 순수 텍스트만 출력.
            // --dangerously-skip-permissions: 비대화 환경이라 권한 프롬프트를 띄울 수 없으므로 건너뜀.
            String npxBin = IS_WINDOWS ? "npx.cmd" : "npx";
            ProcessBuilder pb = new ProcessBuilder(
                    npxBin, "-y", "@anthropic-ai/claude-code",
                    "--print",
                    "--output-format", "text",
                    "--dangerously-skip-permissions"
            );
            pb.redirectInput(tempFile.toFile());   // 프롬프트 파일을 자식의 stdin으로 연결.
            pb.redirectErrorStream(false);          // stderr를 stdout과 분리 — 에러 진단을 응답 본문과 섞지 않기 위함.

            log.info("Claude CLI 실행 시작 (prompt={}자, timeout={}초)", prompt.length(), timeoutSeconds);
            process = pb.start();

            // stdout/stderr를 각각 별도 스레드에서 동시에 비운다.
            // 한쪽만 끝까지 읽으면 다른 쪽 파이프 버퍼(보통 ~64KB)가 가득 차서
            // 자식 프로세스가 write에서 멈추고, 그 결과 응답이 끝나지 않아
            // '타임아웃'처럼 보이는 교착(deadlock)이 발생할 수 있다.
            CompletableFuture<String> stdoutFuture = readAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readAsync(process.getErrorStream());

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                long elapsedMs = elapsedMs(startNanos);
                String stderr = await(stderrFuture);
                log.error("Claude CLI 응답 시간 초과 — {}초 제한, 실제 {}ms 경과 후 프로세스 강제 종료. stderr: {}",
                        timeoutSeconds, elapsedMs, abbreviate(stderr));
                throw new RuntimeException("Claude CLI 응답 시간 초과 (" + timeoutSeconds + "초)");
            }

            String stdout = await(stdoutFuture);
            String stderr = await(stderrFuture);
            long elapsedMs = elapsedMs(startNanos);

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Claude CLI 비정상 종료 (exitCode={}, {}ms 경과). stderr: {}",
                        exitCode, elapsedMs, abbreviate(stderr));
                throw new RuntimeException("Claude CLI 실행 실패 (exitCode=" + exitCode + ")");
            }

            log.info("Claude CLI 실행 완료 ({}ms 경과, 응답 {}자)", elapsedMs, stdout.length());
            return stdout.trim();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Claude CLI 실행 중 인터럽트 ({}ms 경과)", elapsedMs(startNanos));
            throw new RuntimeException("Claude CLI 실행 중 인터럽트 발생", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude CLI 호출 실패 ({}ms 경과): {}", elapsedMs(startNanos), e.getMessage(), e);
            throw new RuntimeException("Claude CLI 호출 실패", e);
        } finally {
            // 예외/타임아웃 등으로 빠져나갈 때 좀비 프로세스가 남지 않도록 정리.
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) { /* 정리 실패는 무시 */ }
            }
        }
    }

    /** 스트림을 별도 스레드에서 끝까지 읽어 문자열로 모은다. */
    private CompletableFuture<String> readAsync(InputStream in) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                return "";
            }
        });
    }

    /** 스트림 읽기 결과를 제한 시간 안에서 가져온다. 못 받으면 빈 문자열로 폴백. */
    private String await(CompletableFuture<String> future) {
        try {
            return future.get(STREAM_DRAIN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String abbreviate(String s) {
        if (s == null || s.isBlank()) return "(없음)";
        String trimmed = s.strip();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) + "…(생략)" : trimmed;
    }
}