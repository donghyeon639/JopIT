package com.main.jobit.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

// 로컬 캐시(Caffeine) + 스케줄링 활성화 설정.
// @EnableCaching: @Cacheable 등 캐시 어노테이션 동작 활성화.
// @EnableScheduling: 외부 데이터 수집용 @Scheduled 작업(채용 동기화, RSS 수집) 동작 활성화.
// Redis 등 분산 캐시는 미도입 — 단일 인스턴스 기준 인메모리 캐시로 충분하다는 판단.
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    // 명시적으로 선언한 두 캐시("techTrends", "jobPostings")만 사용. 등록되지 않은 이름은 캐시되지 않는다.
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("techTrends", "jobPostings");
        // 쓰기 후 5분 만료 + 최대 50개 엔트리. 외부 API 응답을 짧게 캐싱해 반복 호출/요금을 줄이는 용도.
        // (스케줄러가 주기적으로 원본을 갱신하므로 캐시 TTL은 짧게 둬도 무방)
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(50));
        return manager;
    }
}