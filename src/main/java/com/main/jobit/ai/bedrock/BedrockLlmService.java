package com.main.jobit.ai.bedrock;

import com.main.jobit.ai.port.LlmPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.stream.Collectors;

// LlmPort의 유일한 구현 어댑터. AWS SDK v2 BedrockRuntime의 Converse API로 Claude를 호출한다.
// 인증은 EC2 IAM 역할(DefaultCredentialsProvider 체인)이 자동 처리 — API 키/시크릿 불필요(CLAUDE.md §4.4).
//
// 현재는 generate(단발)만 구현. 스트리밍·tool use(converse)는 챗봇/화상면접 단계에서 확장한다 —
// Converse API가 멀티턴·도구 호출을 같은 계약으로 다루므로 그때 이 클래스에 메서드만 추가하면 된다.
@Slf4j
@Service
public class BedrockLlmService implements LlmPort {

    // 호출할 모델 ID(또는 추론 프로파일 ID). 리전별 가용성이 달라 코드에 박지 않고 외부 설정으로 받는다.
    // 미설정(빈 값)이어도 컨텍스트 로드는 되도록 두되(테스트·부팅 보호), 부팅 시 경고하고 실제 호출 시 명확히 실패시킨다.
    @Value("${ai.bedrock.model-id:}")
    private String modelId;

    // 모델 호출 리전. 서울(ap-northeast-2) 기본. 해당 리전에 모델이 없으면 설정으로 다른 리전 우회.
    @Value("${ai.bedrock.region:ap-northeast-2}")
    private String region;

    // 응답 최대 토큰. 단발 피드백/질문 생성 기준 기본값.
    @Value("${ai.bedrock.max-tokens:4096}")
    private int maxTokens;

    // BedrockRuntimeClient는 스레드 안전하므로 빈 생성 시 1회만 만들어 공유한다.
    // build() 시점엔 자격증명을 조회하지 않고(요청 시 지연 해석) 네트워크 호출도 없어, AWS 미구성 환경(테스트)에서도 안전하다.
    private BedrockRuntimeClient client;

    @PostConstruct
    void init() {
        this.client = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .build();
        if (modelId == null || modelId.isBlank()) {
            log.warn("BedrockLlmService 초기화됨 — ai.bedrock.model-id가 비어 있음. 실제 AI 호출 시 실패한다. (region={})", region);
        } else {
            log.info("BedrockLlmService 초기화 (region={}, modelId={}, maxTokens={})", region, modelId, maxTokens);
        }
    }

    @PreDestroy
    void close() {
        if (client != null) {
            client.close();
        }
    }

    // LlmPort 계약 구현 — 프롬프트를 USER 메시지로 보내고 응답의 텍스트 블록들을 합쳐 반환한다.
    // 실패는 RuntimeException으로 변환 — 호출측(@Async 서비스)의 기존 try/catch 흐름을 그대로 탄다.
    @Override
    public String generate(String prompt) {
        // 설정 누락은 빈 생성이 아니라 호출 시점에 막는다(부팅·테스트는 통과시키되, 미구성 상태로 호출하면 명확히 실패).
        if (modelId == null || modelId.isBlank()) {
            throw new IllegalStateException("ai.bedrock.model-id가 설정되지 않았습니다 — Bedrock 모델 ID를 설정하세요.");
        }

        long startNanos = System.nanoTime();
        try {
            ConverseResponse response = client.converse(req -> req
                    .modelId(modelId)
                    .messages(Message.builder()
                            .role(ConversationRole.USER)
                            .content(ContentBlock.fromText(prompt))
                            .build())
                    // 샘플링 파라미터(temperature/top_p 등)는 설정하지 않는다 — 최신 Claude 모델은 이를 거부할 수 있다.
                    .inferenceConfig(c -> c.maxTokens(maxTokens)));

            // 응답 content는 여러 블록일 수 있고 텍스트가 아닌 블록의 .text()는 null이므로 걸러서 합친다.
            String text = response.output().message().content().stream()
                    .map(ContentBlock::text)
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.joining("\n"))
                    .trim();

            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("Bedrock 호출 완료 ({}ms 경과, 응답 {}자)", elapsedMs, text.length());
            return text;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.error("Bedrock 호출 실패 ({}ms 경과): {}", elapsedMs, e.getMessage(), e);
            throw new RuntimeException("Bedrock 호출 실패", e);
        }
    }
}