package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;
import com.core.test.utils.TestSupport;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * User 엔티티를 위한 Fixture
 * test-utils 기반으로 리팩토링되었습니다.
 */
public class UserFixture extends BaseFixture<User, User.UserBuilder> {
    
    private static final UserFixture INSTANCE = new UserFixture();
    
    private UserFixture() {
        super(User.class);
    }
    
    public static UserFixture user() {
        return INSTANCE;
    }
    
    @Override
    protected User.UserBuilder defaultBuilder() {
        // 하위 호환성을 위해 유지, 실제로는 사용되지 않음
        return User.builder()
                .username("user" + nextGlobalId())
                .email("user" + nextGlobalId() + "@example.com")
                .password("password123")
                .roles(new HashSet<>(1));
    }
    
    @Override
    protected User buildFrom(User.UserBuilder builder) {
        // 하위 호환성을 위해 유지, 실제로는 사용되지 않음
        return builder.build();
    }
    
    @Override
    protected void applyCustomization(User entity, Consumer<User.UserBuilder> customizer) {
        // 빌더를 사용하여 커스터마이징을 엔티티에 적용
        User.UserBuilder builder = User.builder()
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .roles(entity.getRoles());
        customizer.accept(builder);
        User customized = builder.build();
        
        // 엔티티에 변경사항 적용
        entity.setUsername(customized.getUsername());
        entity.setEmail(customized.getEmail());
        entity.setPassword(customized.getPassword());
        entity.setRoles(customized.getRoles());
    }
    
    @Override
    public User aDefault() {
        // 빌더를 사용하여 기본 User 생성
        User user = defaultBuilder().build();
        user.setId(nextId());
        return user;
    }
    
    // 편의 메서드들
    public static User aUser() {
        return user().aDefault();
    }
    
    public static User anAdmin() {
        User user = user().aDefault();
        user.setRoles(new HashSet<>(2));
        return user;
    }
    
    public static User aUserWith(String username) {
        User user = user().aDefault();
        user.setUsername(username);
        return user;
    }
    
    public static User aUserWith(String username, String email) {
        User user = user().aDefault();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
    
    public static User aUserWithRole(Set<Role> roles) {
        User user = user().aDefault();
        user.setRoles(roles);
        return user;
    }
}

