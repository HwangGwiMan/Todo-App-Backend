package com.TodoApp.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정
 * Spring Events의 비동기 처리를 위해 필요합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}

