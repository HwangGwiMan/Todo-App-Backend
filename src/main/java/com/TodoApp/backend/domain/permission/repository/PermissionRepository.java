package com.TodoApp.backend.domain.permission.repository;

import com.TodoApp.backend.domain.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findByResourceAndAction(
        Permission.Resource resource, 
        Permission.Action action
    );
    boolean existsByName(String name);
}
