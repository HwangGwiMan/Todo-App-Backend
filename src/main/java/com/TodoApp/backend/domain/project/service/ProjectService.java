package com.TodoApp.backend.domain.project.service;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
import com.TodoApp.backend.domain.project.dto.ProjectResponse;
import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.project.repository.ProjectRepository;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.exception.BusinessException;
import com.TodoApp.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로젝트 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TodoRepository todoRepository;

    /**
     * 사용자별 프로젝트 목록 조회
     */
    public List<ProjectResponse> getProjectsByUser(User user) {
        List<Project> projects = projectRepository.findByUserOrderByPositionAscCreatedAtAsc(user);
        
        return projects.stream()
                .map(project -> {
                    Long todoCount = todoRepository.countByUserAndProjectId(user, project.getId());
                    return ProjectResponse.fromWithTodoCount(project, todoCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 상세 조회
     */
    public ProjectResponse getProject(Long projectId, User user) {
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        
        Long todoCount = todoRepository.countByUserAndProjectId(user, projectId);
        return ProjectResponse.fromWithTodoCount(project, todoCount);
    }

    /**
     * 프로젝트 생성
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User user) {
        // 프로젝트명 중복 체크
        if (projectRepository.existsByUserAndName(user, request.getName())) {
            throw new BusinessException(ErrorCode.PROJECT_NAME_DUPLICATE);
        }

        // 기본 프로젝트 설정 시 기존 기본 프로젝트 해제
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            projectRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(defaultProject -> {
                        defaultProject.setIsDefault(false);
                        projectRepository.save(defaultProject);
                    });
        }

        // position 자동 설정
        Integer nextPosition = request.getPosition();
        if (nextPosition == null) {
            nextPosition = projectRepository.findMaxPositionByUser(user) + 1;
        }

        Project project = Project.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3B82F6")
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .position(nextPosition)
                .build();

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.from(savedProject);
    }

    /**
     * 프로젝트 수정
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, User user) {
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트명 중복 체크 (자신 제외)
        if (projectRepository.existsByUserAndNameExcludingId(user, request.getName(), projectId)) {
            throw new BusinessException(ErrorCode.PROJECT_NAME_DUPLICATE);
        }

        // 기본 프로젝트 설정 시 기존 기본 프로젝트 해제
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(project.getIsDefault())) {
            projectRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(defaultProject -> {
                        defaultProject.setIsDefault(false);
                        projectRepository.save(defaultProject);
                    });
        }

        // 프로젝트 정보 업데이트
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }
        if (request.getIsDefault() != null) {
            project.setIsDefault(request.getIsDefault());
        }
        if (request.getPosition() != null) {
            project.setPosition(request.getPosition());
        }

        Project updatedProject = projectRepository.save(project);
        Long todoCount = todoRepository.countByUserAndProjectId(user, projectId);
        
        return ProjectResponse.fromWithTodoCount(updatedProject, todoCount);
    }

    /**
     * 프로젝트 삭제
     */
    @Transactional
    public void deleteProject(Long projectId, User user) {
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 기본 프로젝트는 삭제할 수 없음
        if (Boolean.TRUE.equals(project.getIsDefault())) {
            throw new BusinessException(ErrorCode.DEFAULT_PROJECT_DELETE_NOT_ALLOWED);
        }

        // 프로젝트 내 모든 TODO의 projectId를 null로 변경
        todoRepository.updateProjectIdToNullByProjectId(projectId);

        // 프로젝트 삭제
        projectRepository.delete(project);
    }

    /**
     * 사용자의 기본 프로젝트 조회
     */
    public ProjectResponse getDefaultProject(User user) {
        Project project = projectRepository.findByUserAndIsDefaultTrue(user)
                .orElse(null);
        
        if (project == null) {
            return null;
        }
        
        Long todoCount = todoRepository.countByUserAndProjectId(user, project.getId());
        return ProjectResponse.fromWithTodoCount(project, todoCount);
    }

    /**
     * 기본 프로젝트 생성 (사용자 회원가입 시)
     */
    @Transactional
    public ProjectResponse createDefaultProject(User user) {
        // 이미 기본 프로젝트가 있는지 확인
        if (projectRepository.findByUserAndIsDefaultTrue(user).isPresent()) {
            throw new IllegalStateException("이미 기본 프로젝트가 존재합니다.");
        }

        Project defaultProject = Project.builder()
                .user(user)
                .name("기본 프로젝트")
                .description("기본적으로 생성된 프로젝트입니다.")
                .color("#3B82F6")
                .isDefault(true)
                .position(0)
                .build();

        Project savedProject = projectRepository.save(defaultProject);
        return ProjectResponse.from(savedProject);
    }
}
