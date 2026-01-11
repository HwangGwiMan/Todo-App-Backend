package com.TodoApp.backend.global.config;

import com.TodoApp.backend.domain.permission.entity.Permission;
import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.permission.repository.PermissionRepository;
import com.TodoApp.backend.domain.permission.repository.RoleRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Role과 Permission 초기 데이터 설정
 * 애플리케이션 시작 시 기본 권한과 역할을 생성합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RolePermissionInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Role과 Permission 초기 데이터 설정 시작...");
        
        // 1. 기본 Permission 생성
        Set<Permission> permissions = createPermissions();
        
        // 2. 기본 Role 생성 및 Permission 할당
        createRoles(permissions);
        
        // 3. 기존 사용자에 USER 역할 할당
        assignDefaultRoleToExistingUsers();
        
        log.info("Role과 Permission 초기 데이터 설정 완료");
    }

    private Set<Permission> createPermissions() {
        Set<Permission> permissions = new HashSet<>();
        
        // TODO 권한
        permissions.add(createPermissionIfNotExists("TODO_READ", "TODO 조회", Permission.Resource.TODO, Permission.Action.READ));
        permissions.add(createPermissionIfNotExists("TODO_WRITE", "TODO 생성/수정", Permission.Resource.TODO, Permission.Action.WRITE));
        permissions.add(createPermissionIfNotExists("TODO_DELETE", "TODO 삭제", Permission.Resource.TODO, Permission.Action.DELETE));
        
        // PROJECT 권한
        permissions.add(createPermissionIfNotExists("PROJECT_READ", "프로젝트 조회", Permission.Resource.PROJECT, Permission.Action.READ));
        permissions.add(createPermissionIfNotExists("PROJECT_WRITE", "프로젝트 생성/수정", Permission.Resource.PROJECT, Permission.Action.WRITE));
        permissions.add(createPermissionIfNotExists("PROJECT_DELETE", "프로젝트 삭제", Permission.Resource.PROJECT, Permission.Action.DELETE));
        
        // USER 권한
        permissions.add(createPermissionIfNotExists("USER_READ", "사용자 조회", Permission.Resource.USER, Permission.Action.READ));
        permissions.add(createPermissionIfNotExists("USER_MANAGE", "사용자 관리", Permission.Resource.USER, Permission.Action.MANAGE));
        
        // ADMIN 권한
        permissions.add(createPermissionIfNotExists("ADMIN_ACCESS", "관리자 접근", Permission.Resource.ADMIN, Permission.Action.READ));
        
        return permissions;
    }

    private Permission createPermissionIfNotExists(String name, String description, Permission.Resource resource, Permission.Action action) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = Permission.builder()
                            .name(name)
                            .description(description)
                            .resource(resource)
                            .action(action)
                            .build();
                    Permission saved = permissionRepository.save(permission);
                    log.info("Permission 생성: {}", name);
                    return saved;
                });
    }

    private void createRoles(Set<Permission> permissions) {
        // USER 역할 생성
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("USER")
                            .description("일반 사용자 역할")
                            .build();
                    Role saved = roleRepository.save(role);
                    log.info("Role 생성: USER");
                    return saved;
                });
        
        // USER 역할에 권한 할당
        Set<Permission> userPermissions = permissions.stream()
                .filter(p -> p.getName().startsWith("TODO_") || p.getName().startsWith("PROJECT_"))
                .collect(java.util.stream.Collectors.toSet());
        
        if (userRole.getPermissions() == null) {
            userRole.setPermissions(new HashSet<>());
        }
        userRole.getPermissions().clear();
        userRole.getPermissions().addAll(userPermissions);
        roleRepository.save(userRole);
        log.info("USER 역할에 {}개의 권한 할당", userPermissions.size());
        
        // ADMIN 역할 생성
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("ADMIN")
                            .description("관리자 역할")
                            .build();
                    Role saved = roleRepository.save(role);
                    log.info("Role 생성: ADMIN");
                    return saved;
                });
        
        // ADMIN 역할에 모든 권한 할당
        if (adminRole.getPermissions() == null) {
            adminRole.setPermissions(new HashSet<>());
        }
        adminRole.getPermissions().clear();
        adminRole.getPermissions().addAll(permissions);
        roleRepository.save(adminRole);
        log.info("ADMIN 역할에 {}개의 권한 할당", permissions.size());
    }

    private void assignDefaultRoleToExistingUsers() {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER 역할이 존재하지 않습니다."));
        
        userRepository.findAll().forEach(user -> {
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                if (user.getRoles() == null) {
                    user.setRoles(new HashSet<>());
                }
                user.getRoles().add(userRole);
                userRepository.save(user);
                log.info("기존 사용자 '{}'에 USER 역할 할당", user.getUsername());
            }
        });
    }
}
