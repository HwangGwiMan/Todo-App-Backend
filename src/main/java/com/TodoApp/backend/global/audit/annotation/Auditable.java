package com.TodoApp.backend.global.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로그를 기록할 메서드에 적용하는 어노테이션
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Auditable(action = "CREATE")
 * public TodoResponse createTodo(Long userId, TodoRequest request) {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * 수행된 액션 (예: "CREATE", "UPDATE", "DELETE")
     */
    String action();
    
    /**
     * 엔티티 이름 (예: "Todo", "Project")
     * 지정하지 않으면 메서드의 반환 타입이나 파라미터에서 자동 추출 시도
     */
    String entityName() default "";
}
