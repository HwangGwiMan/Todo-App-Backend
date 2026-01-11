package com.TodoApp.backend.domain.permission.entity;

import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * 역할(Role) 엔티티
 * 여러 권한(Permission)을 그룹화하여 사용자에게 할당합니다.
 */
@Entity
@Table(name = "roles")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 255)
    private String description;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
