package com.TodoApp.backend.domain.permission.service;

import com.TodoApp.backend.domain.permission.dto.RoleRequest;
import com.TodoApp.backend.domain.permission.dto.RoleResponse;
import com.TodoApp.backend.domain.permission.entity.Permission;
import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.permission.repository.PermissionRepository;
import com.TodoApp.backend.domain.permission.repository.RoleRepository;
import com.TodoApp.backend.global.exception.BusinessException;
import com.TodoApp.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 역할(Role) 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 모든 역할 조회
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 역할 상세 조회
     */
    @Transactional(readOnly = true)
    public RoleResponse getRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.from(role);
    }

    /**
     * 역할 생성
     */
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        // 역할명 중복 확인
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_DUPLICATE);
        }

        // 역할 생성
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 권한 할당
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds())
            );
            role.setPermissions(permissions);
        } else {
            role.setPermissions(new HashSet<>());
        }

        Role savedRole = roleRepository.save(role);
        return RoleResponse.from(savedRole);
    }

    /**
     * 역할 수정
     */
    @Transactional
    public RoleResponse updateRole(Long roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        // 기본 역할 삭제 방지 (이름 변경 방지)
        if (isDefaultRole(role.getName()) && !role.getName().equals(request.getName())) {
            throw new BusinessException(ErrorCode.DEFAULT_ROLE_DELETE_NOT_ALLOWED, "기본 역할의 이름은 변경할 수 없습니다.");
        }

        // 역할명 중복 확인 (자기 자신 제외)
        if (!role.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_DUPLICATE);
        }

        // 역할 정보 업데이트
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // 권한 업데이트
        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds())
            );
            role.setPermissions(permissions);
        } else {
            role.setPermissions(new HashSet<>());
        }

        Role updatedRole = roleRepository.save(role);
        return RoleResponse.from(updatedRole);
    }

    /**
     * 역할 삭제
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        // 기본 역할 삭제 방지
        if (isDefaultRole(role.getName())) {
            throw new BusinessException(ErrorCode.DEFAULT_ROLE_DELETE_NOT_ALLOWED);
        }

        roleRepository.delete(role);
    }

    /**
     * 기본 역할인지 확인
     */
    private boolean isDefaultRole(String roleName) {
        return "USER".equals(roleName) || "ADMIN".equals(roleName);
    }
}
