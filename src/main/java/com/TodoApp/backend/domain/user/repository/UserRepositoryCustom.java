package com.TodoApp.backend.domain.user.repository;

import com.TodoApp.backend.domain.user.entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<User> findUserWithRolesAndPermissionsByUsername(String username);
}
