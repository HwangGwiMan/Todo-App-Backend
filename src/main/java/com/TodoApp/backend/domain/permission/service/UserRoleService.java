package com.TodoApp.backend.domain.permission.service;

import com.TodoApp.backend.domain.permission.dto.RoleResponse;
import com.TodoApp.backend.domain.permission.dto.UserRoleRequest;
import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.permission.repository.RoleRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
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
 * 사용자 역할 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * 사용자의 역할 목록 조회
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        if (user.getRoles() == null) {
            return List.of();
        }
        
        return user.getRoles().stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자에 역할 할당
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * 사용자에서 역할 제거
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new BusinessException(ErrorCode.USER_MUST_HAVE_ONE_ROLE);
        }
        
        // 최소 1개의 역할은 유지해야 함
        if (user.getRoles().size() <= 1) {
            throw new BusinessException(ErrorCode.USER_MUST_HAVE_ONE_ROLE);
        }
        
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    /**
     * 사용자 역할 일괄 업데이트
     */
    @Transactional
    public void updateUserRoles(Long userId, UserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 최소 1개의 역할은 필수
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new BusinessException(ErrorCode.USER_MUST_HAVE_ONE_ROLE);
        }
        
        // 역할 조회
        Set<Role> roles = new HashSet<>(
                roleRepository.findAllById(request.getRoleIds())
        );
        
        // 요청한 역할 ID 중 일부가 존재하지 않는 경우
        if (roles.size() != request.getRoleIds().size()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "일부 역할을 찾을 수 없습니다.");
        }
        
        user.setRoles(roles);
        userRepository.save(user);
    }
}
