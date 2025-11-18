package com.TodoApp.backend.domain.project.service;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.project.repository.ProjectRepository;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 테스트")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private Project testProject;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(User.Role.USER)
                .build();
        testUser.setId(1L);

        testProject = Project.builder()
                .user(testUser)
                .name("테스트 프로젝트")
                .description("테스트 설명")
                .color("#3B82F6")
                .isDefault(false)
                .position(0)
                .build();
        testProject.setId(1L);

        projectRequest = new ProjectRequest();
        projectRequest.setName("새로운 프로젝트");
        projectRequest.setDescription("새로운 설명");
        projectRequest.setColor("#FF0000");
        projectRequest.setIsDefault(false);
        projectRequest.setPosition(0);
    }

    @Test
    @DisplayName("사용자별 프로젝트 목록 조회 성공")
    void getProjectsByUser_성공() {
        // Given
        Project project2 = Project.builder()
                .user(testUser)
                .name("프로젝트 2")
                .build();
        project2.setId(2L);

        List<Project> projects = Arrays.asList(testProject, project2);
        when(projectRepository.findByUserOrderByPositionAscCreatedAtAsc(testUser)).thenReturn(projects);
        when(todoRepository.countByUserAndProjectId(testUser, 1L)).thenReturn(5L);
        when(todoRepository.countByUserAndProjectId(testUser, 2L)).thenReturn(3L);

        // When
        List<com.TodoApp.backend.domain.project.dto.ProjectResponse> response = projectService.getProjectsByUser(testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("테스트 프로젝트");
        assertThat(response.get(0).getTodoCount()).isEqualTo(5L);
        verify(projectRepository).findByUserOrderByPositionAscCreatedAtAsc(testUser);
        verify(todoRepository, times(2)).countByUserAndProjectId(any(User.class), anyLong());
    }

    @Test
    @DisplayName("프로젝트 상세 조회 성공")
    void getProject_성공() {
        // Given
        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testProject));
        when(todoRepository.countByUserAndProjectId(testUser, 1L)).thenReturn(5L);

        // When
        var response = projectService.getProject(1L, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("테스트 프로젝트");
        assertThat(response.getTodoCount()).isEqualTo(5L);
        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository).countByUserAndProjectId(testUser, 1L);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 실패 - 프로젝트 없음")
    void getProject_실패_없음() {
        // Given
        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProject(1L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");

        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository, never()).countByUserAndProjectId(any(), anyLong());
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_성공() {
        // Given
        Project newProject = Project.builder()
                .user(testUser)
                .name("새로운 프로젝트")
                .description("새로운 설명")
                .color("#FF0000")
                .isDefault(false)
                .position(1)
                .build();
        newProject.setId(2L);

        when(projectRepository.existsByUserAndName(testUser, "새로운 프로젝트")).thenReturn(false);
        when(projectRepository.findMaxPositionByUser(testUser)).thenReturn(0);
        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        // When
        var response = projectService.createProject(projectRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("새로운 프로젝트");
        verify(projectRepository).existsByUserAndName(testUser, "새로운 프로젝트");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 이름 중복")
    void createProject_실패_이름_중복() {
        // Given
        when(projectRepository.existsByUserAndName(testUser, "새로운 프로젝트")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(projectRequest, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 프로젝트명입니다.");

        verify(projectRepository).existsByUserAndName(testUser, "새로운 프로젝트");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 - 기본 프로젝트 설정 시 기존 기본 프로젝트 해제")
    void createProject_기본_프로젝트_설정() {
        // Given
        Project existingDefaultProject = Project.builder()
                .user(testUser)
                .name("기존 기본 프로젝트")
                .isDefault(true)
                .build();
        existingDefaultProject.setId(2L);

        ProjectRequest defaultProjectRequest = new ProjectRequest();
        defaultProjectRequest.setName("새로운 기본 프로젝트");
        defaultProjectRequest.setIsDefault(true);

        Project newDefaultProject = Project.builder()
                .user(testUser)
                .name("새로운 기본 프로젝트")
                .isDefault(true)
                .position(1)
                .build();
        newDefaultProject.setId(3L);

        when(projectRepository.existsByUserAndName(testUser, "새로운 기본 프로젝트")).thenReturn(false);
        when(projectRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(existingDefaultProject));
        when(projectRepository.findMaxPositionByUser(testUser)).thenReturn(0);
        when(projectRepository.save(any(Project.class))).thenReturn(newDefaultProject);

        // When
        var response = projectService.createProject(defaultProjectRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getIsDefault()).isTrue();
        verify(projectRepository).findByUserAndIsDefaultTrue(testUser);
        verify(projectRepository, times(2)).save(any(Project.class)); // 기존 기본 프로젝트 해제 + 새 프로젝트 생성
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    void updateProject_성공() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("수정된 프로젝트");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setColor("#00FF00");
        updateRequest.setIsDefault(false);

        Project updatedProject = Project.builder()
                .user(testUser)
                .name("수정된 프로젝트")
                .description("수정된 설명")
                .color("#00FF00")
                .isDefault(false)
                .build();
        updatedProject.setId(1L);

        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testProject));
        when(projectRepository.existsByUserAndNameExcludingId(testUser, "수정된 프로젝트", 1L)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        when(todoRepository.countByUserAndProjectId(testUser, 1L)).thenReturn(5L);

        // When
        var response = projectService.updateProject(1L, updateRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("수정된 프로젝트");
        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(projectRepository).existsByUserAndNameExcludingId(testUser, "수정된 프로젝트", 1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 이름 중복")
    void updateProject_실패_이름_중복() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("중복된 프로젝트명");

        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testProject));
        when(projectRepository.existsByUserAndNameExcludingId(testUser, "중복된 프로젝트명", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(1L, updateRequest, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 프로젝트명입니다.");

        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(projectRepository).existsByUserAndNameExcludingId(testUser, "중복된 프로젝트명", 1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_성공() {
        // Given
        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testProject));
        doNothing().when(todoRepository).updateProjectIdToNullByProjectId(1L);
        doNothing().when(projectRepository).delete(any(Project.class));

        // When
        projectService.deleteProject(1L, testUser);

        // Then
        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository).updateProjectIdToNullByProjectId(1L);
        verify(projectRepository).delete(testProject);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 기본 프로젝트")
    void deleteProject_실패_기본_프로젝트() {
        // Given
        Project defaultProject = Project.builder()
                .user(testUser)
                .name("기본 프로젝트")
                .isDefault(true)
                .build();
        defaultProject.setId(1L);

        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(defaultProject));

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(1L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기본 프로젝트는 삭제할 수 없습니다.");

        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository, never()).updateProjectIdToNullByProjectId(anyLong());
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("기본 프로젝트 조회 성공")
    void getDefaultProject_성공() {
        // Given
        Project defaultProject = Project.builder()
                .user(testUser)
                .name("기본 프로젝트")
                .isDefault(true)
                .build();
        defaultProject.setId(1L);

        when(projectRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(defaultProject));
        when(todoRepository.countByUserAndProjectId(testUser, 1L)).thenReturn(10L);

        // When
        var response = projectService.getDefaultProject(testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("기본 프로젝트");
        assertThat(response.getIsDefault()).isTrue();
        assertThat(response.getTodoCount()).isEqualTo(10L);
        verify(projectRepository).findByUserAndIsDefaultTrue(testUser);
        verify(todoRepository).countByUserAndProjectId(testUser, 1L);
    }

    @Test
    @DisplayName("기본 프로젝트 조회 - 없음")
    void getDefaultProject_없음() {
        // Given
        when(projectRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.empty());

        // When
        var response = projectService.getDefaultProject(testUser);

        // Then
        assertThat(response).isNull();
        verify(projectRepository).findByUserAndIsDefaultTrue(testUser);
        verify(todoRepository, never()).countByUserAndProjectId(any(), anyLong());
    }

    @Test
    @DisplayName("기본 프로젝트 생성 성공")
    void createDefaultProject_성공() {
        // Given
        Project defaultProject = Project.builder()
                .user(testUser)
                .name("기본 프로젝트")
                .description("기본적으로 생성된 프로젝트입니다.")
                .color("#3B82F6")
                .isDefault(true)
                .position(0)
                .build();
        defaultProject.setId(1L);

        when(projectRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.empty());
        when(projectRepository.save(any(Project.class))).thenReturn(defaultProject);

        // When
        var response = projectService.createDefaultProject(testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("기본 프로젝트");
        assertThat(response.getIsDefault()).isTrue();
        verify(projectRepository).findByUserAndIsDefaultTrue(testUser);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("기본 프로젝트 생성 실패 - 이미 존재")
    void createDefaultProject_실패_이미_존재() {
        // Given
        Project existingDefaultProject = Project.builder()
                .user(testUser)
                .name("기존 기본 프로젝트")
                .isDefault(true)
                .build();
        existingDefaultProject.setId(1L);

        when(projectRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(existingDefaultProject));

        // When & Then
        assertThatThrownBy(() -> projectService.createDefaultProject(testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 기본 프로젝트가 존재합니다.");

        verify(projectRepository).findByUserAndIsDefaultTrue(testUser);
        verify(projectRepository, never()).save(any(Project.class));
    }
}

