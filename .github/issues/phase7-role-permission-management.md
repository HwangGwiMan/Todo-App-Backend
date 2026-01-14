# [Phase 7] DB 기반 Role & Permission 관리 시스템

## 상태
✅ **완료** (2025년 12월)

## 개요

데이터베이스에서 Role과 Permission을 관리하는 유연한 권한 관리 시스템을 구축합니다. Spring Security와 통합하여 세밀한 접근 제어를 제공합니다.

## 우선순위
높음 (필수) ✅ 완료

## 예상 소요 시간
12-17시간 (실제 소요: 약 12-15시간, 테스트 제외)

## 목표

- ✅ DB에서 Role과 Permission을 관리
- ✅ 유연한 권한 체계 구축 (RBAC - Role-Based Access Control)
- ✅ Spring Security와 통합
- ✅ 관리자용 권한 관리 API 제공
- ✅ HTTP Method + Permission 기반 접근 제어

## 구현 완료 상태

**완료일:** 2025년 12월

**구현 완료 항목:**
- ✅ Phase 7-1: 엔티티 및 Repository 생성
- ✅ Phase 7-2: 초기 데이터 설정
- ✅ Phase 7-3: Service 계층 구현
- ✅ Phase 7-4: DTO 생성
- ✅ Phase 7-5: Controller 구현
- ✅ Phase 7-6: Security 통합
- ✅ Phase 7-7: 기타 수정 (AuthService, ErrorCode)
- ⏳ Phase 7-8: 테스트 (단위/통합 테스트 작성 예정)

## 데이터베이스 설계

### 엔티티 관계도

```
User (1) ──── (N) UserRole (N) ──── (1) Role (1) ──── (N) RolePermission (N) ──── (1) Permission
```

### 테이블 구조

#### roles 테이블
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### permissions 테이블
```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,  -- 'TODO', 'PROJECT', 'USER' 등
    action VARCHAR(50) NOT NULL,     -- 'READ', 'WRITE', 'DELETE' 등
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### user_roles 테이블 (다대다)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

#### role_permissions 테이블 (다대다)
```sql
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
```

#### users 테이블 수정
```sql
-- 기존 role 컬럼 제거 (선택사항, 마이그레이션 고려)
ALTER TABLE users DROP COLUMN role;
```

## 구현 단계

### Phase 7-1: 엔티티 및 Repository 생성 (2-3시간)

#### 1. Permission 엔티티
```java
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
    private Set<Role> roles = new HashSet<>();
    
    public enum Resource {
        TODO, PROJECT, USER, ADMIN
    }
    
    public enum Action {
        READ, WRITE, DELETE, MANAGE
    }
    
    // 권한 이름 생성 헬퍼 메서드
    public String getPermissionName() {
        return resource.name() + "_" + action.name();
    }
}
```

#### 2. Role 엔티티
```java
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
    private Set<Permission> permissions = new HashSet<>();
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}
```

#### 3. User 엔티티 수정
```java
// Role enum 제거하고 다대다 관계로 변경
@ManyToMany(fetch = FetchType.EAGER)  // 권한 조회를 위해 EAGER
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();

// UserDetails 인터페이스 구현 수정
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    
    // Role 기반 권한 추가
    for (Role role : roles) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        
        // Permission 기반 권한 추가
        for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
        }
    }
    
    return authorities;
}
```

#### 4. Repository 생성
```java
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findByResourceAndAction(
        Permission.Resource resource, 
        Permission.Action action
    );
    boolean existsByName(String name);
}
```

### Phase 7-2: 초기 데이터 설정 (1-2시간)

**DataInitializer 클래스 생성**
- 기본 Permission 생성 (TODO_READ, TODO_WRITE, TODO_DELETE, PROJECT_READ, PROJECT_WRITE, PROJECT_DELETE, USER_READ, USER_MANAGE, ADMIN_ACCESS)
- 기본 Role 생성 (USER, ADMIN)
- Role-Permission 관계 설정
- 기존 사용자에 USER 역할 할당

### Phase 7-3: Service 계층 구현 (2-3시간)

#### RoleService
- `getAllRoles()`: 모든 역할 조회
- `getRole(Long roleId)`: 역할 상세 조회
- `createRole(RoleRequest)`: 역할 생성
- `updateRole(Long roleId, RoleRequest)`: 역할 수정
- `deleteRole(Long roleId)`: 역할 삭제 (기본 역할 삭제 방지)

#### UserRoleService
- `getUserRoles(Long userId)`: 사용자의 역할 목록 조회
- `assignRoleToUser(Long userId, Long roleId)`: 사용자에 역할 할당
- `removeRoleFromUser(Long userId, Long roleId)`: 사용자에서 역할 제거
- `updateUserRoles(Long userId, List<Long> roleIds)`: 사용자 역할 일괄 업데이트

### Phase 7-4: DTO 생성 (1시간)

- `RoleRequest`: 역할 생성/수정 요청
- `RoleResponse`: 역할 응답
- `PermissionResponse`: 권한 응답
- `UserRoleRequest`: 사용자 역할 할당 요청

### Phase 7-5: Controller 구현 (2시간)

#### RoleController
- `GET /api/admin/roles`: 모든 역할 조회
- `GET /api/admin/roles/{id}`: 역할 상세 조회
- `POST /api/admin/roles`: 역할 생성
- `PUT /api/admin/roles/{id}`: 역할 수정
- `DELETE /api/admin/roles/{id}`: 역할 삭제

#### UserRoleController
- `GET /api/admin/users/{userId}/roles`: 사용자 역할 조회
- `POST /api/admin/users/{userId}/roles`: 사용자에 역할 할당
- `DELETE /api/admin/users/{userId}/roles/{roleId}`: 사용자에서 역할 제거
- `PUT /api/admin/users/{userId}/roles`: 사용자 역할 일괄 업데이트

### Phase 7-6: SecurityConfig 업데이트 (1-2시간)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            
            // TODO 권한
            .requestMatchers(HttpMethod.GET, "/api/todos/**")
                .hasAnyAuthority("TODO_READ", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.POST, "/api/todos/**")
                .hasAnyAuthority("TODO_WRITE", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.PUT, "/api/todos/**", HttpMethod.PATCH, "/api/todos/**")
                .hasAnyAuthority("TODO_WRITE", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.DELETE, "/api/todos/**")
                .hasAnyAuthority("TODO_DELETE", "ADMIN_ACCESS")
            
            // PROJECT 권한
            .requestMatchers(HttpMethod.GET, "/api/projects/**")
                .hasAnyAuthority("PROJECT_READ", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.POST, "/api/projects/**")
                .hasAnyAuthority("PROJECT_WRITE", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.PUT, "/api/projects/**")
                .hasAnyAuthority("PROJECT_WRITE", "ADMIN_ACCESS")
            .requestMatchers(HttpMethod.DELETE, "/api/projects/**")
                .hasAnyAuthority("PROJECT_DELETE", "ADMIN_ACCESS")
            
            // 관리자 권한
            .requestMatchers("/api/admin/**")
                .hasAnyRole("ADMIN")
                .hasAuthority("ADMIN_ACCESS")
            
            .anyRequest().authenticated()
        );
    
    return http.build();
}
```

### Phase 7-7: AuthService 수정 (1시간)

회원가입 시 기본 USER 역할 할당

### Phase 7-8: ErrorCode 추가 (30분)

```java
// Role 관련
ROLE_NOT_FOUND(404, "역할을 찾을 수 없습니다."),
ROLE_NAME_DUPLICATE(409, "이미 존재하는 역할명입니다."),
DEFAULT_ROLE_DELETE_NOT_ALLOWED(400, "기본 역할은 삭제할 수 없습니다."),
USER_MUST_HAVE_ONE_ROLE(400, "사용자는 최소 1개의 역할을 가져야 합니다."),

// Permission 관련
PERMISSION_NOT_FOUND(404, "권한을 찾을 수 없습니다."),
```

### Phase 7-9: 테스트 작성 (2-3시간)

- RoleServiceTest
- UserRoleServiceTest
- Security 통합 테스트
- 권한 테스트 시나리오

## 체크리스트

### Phase 7-1: 엔티티 및 Repository ✅
- [x] Permission 엔티티 생성
- [x] Role 엔티티 생성
- [x] User 엔티티 수정 (Role enum → Set<Role>)
- [x] RoleRepository 생성
- [x] PermissionRepository 생성
- [x] 데이터베이스 마이그레이션 스크립트 작성 (JPA DDL 사용)

### Phase 7-2: 초기 데이터 ✅
- [x] RolePermissionInitializer 생성
- [x] 기본 Role 및 Permission 데이터 생성
- [x] 기존 사용자에 USER 역할 할당

### Phase 7-3: Service 계층 ✅
- [x] RoleService 구현
- [x] UserRoleService 구현
- [ ] PermissionService 구현 (선택사항, 현재 미구현)

### Phase 7-4: DTO ✅
- [x] RoleRequest 생성
- [x] RoleResponse 생성
- [x] PermissionResponse 생성
- [x] UserRoleRequest 생성

### Phase 7-5: Controller ✅
- [x] RoleController 생성
- [x] UserRoleController 생성
- [x] Swagger 문서화 (OpenAPI 어노테이션 적용)

### Phase 7-6: Security 통합 ✅
- [x] SecurityConfig 권한 설정 업데이트
- [x] Permission 기반 접근 제어 적용
- [ ] 테스트 (수동 테스트 필요)

### Phase 7-7: 기타 수정 ✅
- [x] AuthService 수정 (회원가입 시 역할 할당)
- [x] ErrorCode 추가
- [x] 기존 코드 마이그레이션 (User 엔티티 Role enum 제거)

### Phase 7-8: 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] 권한 테스트 시나리오 작성

## 예상 소요 시간 상세

- Phase 7-1: 2-3시간 ✅ (완료)
- Phase 7-2: 1-2시간 ✅ (완료)
- Phase 7-3: 2-3시간 ✅ (완료)
- Phase 7-4: 1시간 ✅ (완료)
- Phase 7-5: 2시간 ✅ (완료)
- Phase 7-6: 1-2시간 ✅ (완료)
- Phase 7-7: 1시간 ✅ (완료)
- Phase 7-8: 2-3시간 ⏳ (테스트 작성 예정)

**총 예상 시간: 12-17시간**  
**실제 소요 시간: 약 12-15시간** (테스트 제외)

## 우선순위

**높음 (필수):** ✅ 완료
- Phase 7-1: 엔티티 및 Repository ✅
- Phase 7-2: 초기 데이터 ✅
- Phase 7-3: Service 계층 ✅
- Phase 7-6: Security 통합 ✅

**중간 (권장):** ✅ 완료
- Phase 7-4: DTO ✅
- Phase 7-5: Controller ✅
- Phase 7-7: 기타 수정 ✅

**낮음 (선택):** ⏳ 진행 예정
- Phase 7-8: 테스트 (기본 테스트는 필수, 상세 테스트는 선택)

## 기본 Permission 목록

### TODO 권한
- `TODO_READ`: TODO 조회
- `TODO_WRITE`: TODO 생성/수정
- `TODO_DELETE`: TODO 삭제

### PROJECT 권한
- `PROJECT_READ`: 프로젝트 조회
- `PROJECT_WRITE`: 프로젝트 생성/수정
- `PROJECT_DELETE`: 프로젝트 삭제

### USER 권한
- `USER_READ`: 사용자 조회
- `USER_MANAGE`: 사용자 관리

### ADMIN 권한
- `ADMIN_ACCESS`: 관리자 접근

## 기본 Role 설정

### USER 역할
- TODO_READ, TODO_WRITE, TODO_DELETE
- PROJECT_READ, PROJECT_WRITE, PROJECT_DELETE

### ADMIN 역할
- 모든 권한 포함

## API 엔드포인트

### 역할 관리 (관리자 전용)
- `GET /api/admin/roles`: 모든 역할 조회
- `GET /api/admin/roles/{id}`: 역할 상세 조회
- `POST /api/admin/roles`: 역할 생성
- `PUT /api/admin/roles/{id}`: 역할 수정
- `DELETE /api/admin/roles/{id}`: 역할 삭제

### 사용자 역할 관리 (관리자 전용)
- `GET /api/admin/users/{userId}/roles`: 사용자 역할 조회
- `POST /api/admin/users/{userId}/roles`: 사용자에 역할 할당
- `DELETE /api/admin/users/{userId}/roles/{roleId}`: 사용자에서 역할 제거
- `PUT /api/admin/users/{userId}/roles`: 사용자 역할 일괄 업데이트

## 마이그레이션 전략

1. **기존 사용자 마이그레이션** ✅ 완료
   - `RolePermissionInitializer`가 애플리케이션 시작 시 기존 사용자에 USER 역할 자동 할당
   - JPA DDL을 통해 자동으로 테이블 생성 및 관계 설정

2. **점진적 전환** ✅ 완료
   - User 엔티티에서 `Role` enum 제거 및 `Set<Role> roles`로 완전 전환
   - `getAuthorities()` 메서드에서 `roles`를 기반으로 권한 생성
   - null 안전성 보장을 위한 null 체크 추가

## 참고 자료

- [Spring Security Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
- [RBAC (Role-Based Access Control)](https://en.wikipedia.org/wiki/Role-based_access_control)
- [Spring Data JPA Many-to-Many](https://www.baeldung.com/jpa-many-to-many)

