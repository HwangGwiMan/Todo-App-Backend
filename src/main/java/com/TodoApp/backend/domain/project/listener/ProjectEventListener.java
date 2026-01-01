package com.TodoApp.backend.domain.project.listener;

import com.TodoApp.backend.domain.project.event.ProjectCreatedEvent;
import com.TodoApp.backend.domain.project.event.ProjectDeletedEvent;
import com.TodoApp.backend.domain.project.event.ProjectUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 프로젝트 관련 이벤트 리스너
 * 비동기로 처리되며 트랜잭션 커밋 후 실행됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectEventListener {

    /**
     * 프로젝트 생성 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        log.info("프로젝트 생성됨: projectId={}, name={}, userId={}, username={}",
                event.getProject().getId(),
                event.getProject().getName(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 웰컴 메시지 발송
        // - 통계 업데이트
        // - 감사 로그 기록
    }

    /**
     * 프로젝트 수정 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        log.info("프로젝트 수정됨: projectId={}, name={}, userId={}, username={}",
                event.getProject().getId(),
                event.getProject().getName(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 변경 알림 발송
        // - 통계 업데이트
        // - 감사 로그 기록
    }

    /**
     * 프로젝트 삭제 이벤트 처리
     */
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        log.info("프로젝트 삭제됨: projectId={}, name={}, userId={}, username={}",
                event.getProject().getId(),
                event.getProject().getName(),
                event.getUser().getId(),
                event.getUser().getUsername());
        
        // 향후 확장 가능한 기능들:
        // - 삭제 알림 발송
        // - 통계 업데이트
        // - 감사 로그 기록
    }
}

