package com.TodoApp.backend.domain.user.repository;

import com.TodoApp.backend.domain.permission.entity.Permission;
import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final DSLContext dsl;

    @Override
    public Optional<User> findUserWithRolesAndPermissionsByUsername(String username) {
        // 1. 사용자 정보 조회
        var userRecord = dsl.selectFrom(DSL.table("users"))
            .where(DSL.field("username", String.class).eq(username))
            .fetchOne();

        if (userRecord == null) {
            return Optional.empty();
        }

        User user = User.builder()
            .id(userRecord.get("id", Long.class))
            .username(userRecord.get("username", String.class))
            .email(userRecord.get("email", String.class))
            .password(userRecord.get("password", String.class))
            .roles(new HashSet<>())
            .build();

        // 2. 사용자의 역할 ID 목록 조회
        var roleIds = dsl.select(DSL.field("role_id", Long.class))
            .from(DSL.table("user_roles"))
            .where(DSL.field("user_id", Long.class).eq(user.getId()))
            .fetchInto(Long.class);

        // 3. 각 역할에 대해 순차적으로 조회
        for (Long roleId : roleIds) {
            // 역할 정보 조회
            var roleRecord = dsl.selectFrom(DSL.table("roles"))
                .where(DSL.field("id", Long.class).eq(roleId))
                .fetchOne();

            if (roleRecord != null) {
                Role role = new Role();
                role.setId(roleRecord.get("id", Long.class));
                role.setName(roleRecord.get("name", String.class));
                role.setDescription(roleRecord.get("description", String.class));
                role.setPermissions(new HashSet<>());

                // 4. 역할의 권한 ID 목록 조회
                var permissionIds = dsl.select(DSL.field("permission_id", Long.class))
                    .from(DSL.table("role_permissions"))
                    .where(DSL.field("role_id", Long.class).eq(roleId))
                    .fetchInto(Long.class);

                // 5. 각 권한에 대해 조회
                for (Long permissionId : permissionIds) {
                    var permissionRecord = dsl.selectFrom(DSL.table("permissions"))
                        .where(DSL.field("id", Long.class).eq(permissionId))
                        .fetchOne();

                    if (permissionRecord != null) {
                        Permission permission = Permission.builder()
                            .id(permissionRecord.get("id", Long.class))
                            .name(permissionRecord.get("name", String.class))
                            .description(permissionRecord.get("description", String.class))
                            .resource(Permission.Resource.valueOf(permissionRecord.get("resource", String.class)))
                            .action(Permission.Action.valueOf(permissionRecord.get("action", String.class)))
                            .build();
                        role.getPermissions().add(permission);
                    }
                }

                user.getRoles().add(role);
            }
        }

        return Optional.of(user);
    }
}