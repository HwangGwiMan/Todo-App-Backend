package com.TodoApp.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전역 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common (공통)
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    
    // User (사용자)
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(409, "이미 존재하는 사용자명입니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    
    // Todo (할 일)
    TODO_NOT_FOUND(404, "TODO를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(403, "TODO에 접근할 권한이 없습니다."),
    
    // Project (프로젝트)
    PROJECT_NOT_FOUND(404, "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(403, "프로젝트에 접근할 권한이 없습니다."),
    PROJECT_NAME_DUPLICATE(409, "이미 존재하는 프로젝트명입니다."),
    DEFAULT_PROJECT_DELETE_NOT_ALLOWED(400, "기본 프로젝트는 삭제할 수 없습니다."),
    DEFAULT_PROJECT_NOT_FOUND(404, "기본 프로젝트를 찾을 수 없습니다."),
    
    // Role (역할)
    ROLE_NOT_FOUND(404, "역할을 찾을 수 없습니다."),
    ROLE_NAME_DUPLICATE(409, "이미 존재하는 역할명입니다."),
    DEFAULT_ROLE_DELETE_NOT_ALLOWED(400, "기본 역할은 삭제할 수 없습니다."),
    USER_MUST_HAVE_ONE_ROLE(400, "사용자는 최소 1개의 역할을 가져야 합니다."),
    
    // Permission (권한)
    PERMISSION_NOT_FOUND(404, "권한을 찾을 수 없습니다.");
    
    private final int status;
    private final String message;
}

