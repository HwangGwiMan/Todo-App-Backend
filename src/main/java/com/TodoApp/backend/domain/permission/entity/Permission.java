package com.TodoApp.backend.domain.permission.entity;

import com.TodoApp.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * 권한(Permission) 엔티티
 * 리소스와 액션의 조합으로 세밀한 권한을 정의합니다.
 */
@Entity
@Table(name = "permissions")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 255)
    private String description;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Resource resource;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Action action;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
    
    /**
     * 리소스 타입 enum
     */
    public enum Resource {
        TODO, PROJECT, USER, ADMIN
    }
    
    /**
     * 액션 타입 enum
     */
    public enum Action {
        READ, WRITE, DELETE, MANAGE
    }
    
    /**
     * 권한 이름 생성 헬퍼 메서드
     * 예: "TODO_READ", "PROJECT_WRITE"
     */
    public String getPermissionName() {
        return resource.name() + "_" + action.name();
    }
}
