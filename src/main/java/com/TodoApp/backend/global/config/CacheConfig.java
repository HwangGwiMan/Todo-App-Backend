package com.TodoApp.backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Cache 설정
 * 
 * 캐시 전략:
 * - todos: TODO 단건 조회 캐시
 * - projects: 프로젝트 단건 조회 캐시
 * - projectList: 프로젝트 목록 캐시 (사용자별)
 * - stats: 통계 정보 캐시 (사용자별)
 * 
 * 향후 개선:
 * - 프로덕션 환경에서는 Redis 기반 CacheManager 사용 권장
 * - TTL 설정 추가 (Caffeine 또는 Redis 사용 시)
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        List<ConcurrentMapCache> caches = Arrays.asList(
            new ConcurrentMapCache("todos"),
            new ConcurrentMapCache("projects"),
            new ConcurrentMapCache("projectList"),
            new ConcurrentMapCache("stats")
        );
        
        cacheManager.setCaches(caches);
        cacheManager.afterPropertiesSet();
        
        log.info("CacheManager 초기화 완료: {}개의 캐시 생성", caches.size());
        
        return cacheManager;
    }
}

