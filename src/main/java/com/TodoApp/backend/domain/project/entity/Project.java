package com.TodoApp.backend.domain.project.entity;

import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 엔티티
 * 사용자가 TODO를 그룹화할 수 있는 프로젝트를 표현합니다.
 */
@Entity
@Table(name = "projects")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 7)
    @Builder.Default
    private String color = "#3B82F6"; // 기본 파란색

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false; // 기본 프로젝트 여부

    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0; // 정렬 순서

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (color == null) {
            color = "#3B82F6";
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (position == null) {
            position = 0;
        }
    }

    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();
    }
}
