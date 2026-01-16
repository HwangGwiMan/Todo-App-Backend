package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

import java.util.HashSet;
import java.util.Set;

/**
 * User 엔티티를 위한 Fixture
 */
public class UserFixture extends BaseFixture<User, User.UserBuilder> {
    
    private static final UserFixture INSTANCE = new UserFixture();
    
    public static UserFixture user() {
        return INSTANCE;
    }
    
    @Override
    protected User.UserBuilder defaultBuilder() {
        return User.builder()
                .username("user" + nextGlobalId())
                .email("user" + nextGlobalId() + "@example.com")
                .password("password123")
                .roles(new HashSet<>(1));
    }
    
    @Override
    protected User buildFrom(User.UserBuilder builder) {
        return builder.build();
    }
    
    // 편의 메서드들
    public static User aUser() {
        return user().aDefault();
    }
    
    public static User anAdmin() {
        return user().a(builder -> builder.roles(new HashSet<>(2)));
    }
    
    public static User aUserWith(String username) {
        return user().a(builder -> builder.username(username));
    }
    
    public static User aUserWith(String username, String email) {
        return user().a(builder -> {
            builder.username(username);
            builder.email(email);
        });
    }
    
    public static User aUserWithRole(Set<Role> roles) {
        return user().a(builder -> builder.roles(roles));
    }
}

