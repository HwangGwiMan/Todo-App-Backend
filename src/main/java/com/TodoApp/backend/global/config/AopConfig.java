package com.TodoApp.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 설정
 * @EnableAspectJAutoProxy를 통해 AspectJ 자동 프록시를 활성화합니다.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
}
