package com.TodoApp.backend.domain.todo.entity;

import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    @Column(name = "due_date")
    private Timestamp dueDate;

    @Column(name = "completed_at")
    private Timestamp completedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0;

    // Phase 2에서 사용 예정
    @Column(name = "project_id")
    private Long projectId;

    @Override
    protected void onCreate() {
        super.onCreate();  // BaseEntity의 onCreate() 호출
        if (status == null) {
            status = TodoStatus.TODO;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }
    
    @Override
    protected void onUpdate() {
        super.onUpdate();  // BaseEntity의 onUpdate() 호출
        // 상태가 DONE으로 변경되면 완료 시간 기록
        if (status == TodoStatus.DONE && completedAt == null) {
            completedAt = Timestamp.valueOf(LocalDateTime.now());
        }
        // DONE에서 다른 상태로 변경되면 완료 시간 제거
        if (status != TodoStatus.DONE && completedAt != null) {
            completedAt = null;
        }
    }

    // Todo 상태 Enum
    public enum TodoStatus {
        TODO,           // 할 일
        IN_PROGRESS,    // 진행중
        DONE            // 완료
    }

    // 우선순위 Enum
    public enum Priority {
        HIGH,    // 높음
        MEDIUM,  // 중간
        LOW      // 낮음
    }
}

