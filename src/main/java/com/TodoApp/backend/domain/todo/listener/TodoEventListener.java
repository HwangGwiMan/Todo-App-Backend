package com.TodoApp.backend.domain.todo.listener;

import com.TodoApp.backend.domain.todo.event.TodoCreatedEvent;
import com.TodoApp.backend.domain.todo.event.TodoDeletedEvent;
import com.TodoApp.backend.domain.todo.event.TodoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * TODO 관련 이벤트 리스너
 * 비동기로 처리되며 트랜잭션 커밋 후 실행됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TodoEventListener {

    /**
     * TODO 생성 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoCreated(TodoCreatedEvent event) {
        log.info("TODO 생성됨: todoId={}, title={}, userId={}, username={}",
                event.getTodo().getId(),
                event.getTodo().getTitle(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 알림 발송
        // - 통계 업데이트
        // - 감사 로그 기록
        // - 캐시 무효화
    }

    /**
     * TODO 수정 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoUpdated(TodoUpdatedEvent event) {
        log.info("TODO 수정됨: todoId={}, title={}, userId={}, username={}",
                event.getTodo().getId(),
                event.getTodo().getTitle(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 변경 이력 기록
        // - 알림 발송
        // - 캐시 무효화
    }

    /**
     * TODO 삭제 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoDeleted(TodoDeletedEvent event) {
        log.info("TODO 삭제됨: todoId={}, title={}, userId={}, username={}",
                event.getTodo().getId(),
                event.getTodo().getTitle(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 감사 로그 기록
        // - 관련 데이터 정리
        // - 캐시 무효화
    }
}

