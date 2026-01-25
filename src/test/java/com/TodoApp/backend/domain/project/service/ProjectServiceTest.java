package com.TodoApp.backend.domain.project.service;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
import com.TodoApp.backend.domain.project.dto.ProjectResponse;
import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.project.event.ProjectCreatedEvent;
import com.TodoApp.backend.domain.project.mapper.ProjectMapper;
import com.TodoApp.backend.domain.project.repository.ProjectRepository;
import com.TodoApp.backend.domain.todo.repository.TodoCountByProject;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.ProjectFixture;
import com.TodoApp.backend.fixture.UserFixture;
import com.TodoApp.backend.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 테스트")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private ProjectService projectService;
    
    @Mock
    private ProjectMapper projectMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User testUser;
    private Project testProject;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        // Fixture를 사용하여 테스트 데이터 생성
        testUser = UserFixture.aUser();
        testUser.setId(1L);

        testProject = ProjectFixture.aProjectFor(testUser);
        testProject.setName("테스트 프로젝트");
        testProject.setDescription("테스트 설명");
        testProject.setId(1L);

        projectRequest = new ProjectRequest();
        projectRequest.setName("새로운 프로젝트");
        projectRequest.setDescription("새로운 설명");
        projectRequest.setColor("#FF0000");
        projectRequest.setIsDefault(false);
        projectRequest.setPosition(0);

        // Mapper stub 추가 - toDto 메서드
        lenient().when(projectMapper.toDto(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            return ProjectResponse.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .color(project.getColor())
                    .isDefault(project.getIsDefault())
                    .position(project.getPosition())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .build();
        });

        // Mapper stub 추가 - toDtoWithCount 메서드
        lenient().when(projectMapper.toDtoWithCount(any(Project.class), anyLong())).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            Long todoCount = invocation.getArgument(1);
            ProjectResponse response = ProjectResponse.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .color(project.getColor())
                    .isDefault(project.getIsDefault())
                    .position(project.getPosition())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .todoCount(todoCount)
                    .build();
            return response;
        });

        // Mapper stub 추가 - toEntity 메서드
        lenient().when(projectMapper.toEntity(any(ProjectRequest.class))).thenAnswer(invocation -> {
            ProjectRequest request = invocation.getArgument(0);
            return Project.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .color(request.getColor())
                    .isDefault(request.getIsDefault())
                    .position(request.getPosition())
                    .build();
        });

        // Mapper stub 추가 - updateFromDto 메서드
        lenient().doAnswer(invocation -> {
            ProjectRequest request = invocation.getArgument(0);
            Project project = invocation.getArgument(1);
            if (request.getName() != null) project.setName(request.getName());
            if (request.getDescription() != null) project.setDescription(request.getDescription());
            if (request.getColor() != null) project.setColor(request.getColor());
            if (request.getIsDefault() != null) project.setIsDefault(request.getIsDefault());
            if (request.getPosition() != null) project.setPosition(request.getPosition());
            return null;
        }).when(projectMapper).updateFromDto(any(ProjectRequest.class), any(Project.class));
    }

    @Test
    @DisplayName("사용자별 프로젝트 목록 조회 성공")
    void getProjectsByUser_성공() {
        // Given
        Project project2 = ProjectFixture.aProjectFor(testUser);
        project2.setName("프로젝트 2");
        project2.setId(2L);

        List<Project> projects = Arrays.asList(testProject, project2);
        
        // TodoCountByProject Mock 객체 생성
        TodoCountByProject todoCount1 = mock(TodoCountByProject.class);
        when(todoCount1.getProjectId()).thenReturn(1L);
        when(todoCount1.getCount()).thenReturn(5L);
        
        TodoCountByProject todoCount2 = mock(TodoCountByProject.class);
        when(todoCount2.getProjectId()).thenReturn(2L);
        when(todoCount2.getCount()).thenReturn(3L);
        
        List<TodoCountByProject> todoCountList = Arrays.asList(todoCount1, todoCount2);
        
        when(projectRepository.findByUserOrderByPositionAscCreatedAtAsc(testUser)).thenReturn(projects);
        when(todoRepository.countByUserGroupByProjectId(1L)).thenReturn(todoCountList);

        // When
        List<com.TodoApp.backend.domain.project.dto.ProjectResponse> response = projectService.getProjectsByUser(testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("테스트 프로젝트");
        assertThat(response.get(0).getTodoCount()).isEqualTo(5L);
        assertThat(response.get(1).getName()).isEqualTo("프로젝트 2");
        assertThat(response.get(1).getTodoCount()).isEqualTo(3L);
        verify(projectRepository).findByUserOrderByPositionAscCreatedAtAsc(testUser);
        verify(todoRepository).countByUserGroupByProjectId(1L);
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
                .isInstanceOf(BusinessException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");

        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository, never()).countByUserAndProjectId(any(), anyLong());
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_성공() {
        // Given
        Project newProject = ProjectFixture.aProjectFor(testUser);
        newProject.setName("새로운 프로젝트");
        newProject.setDescription("새로운 설명");
        newProject.setColor("#FF0000");
        newProject.setIsDefault(false);
        newProject.setPosition(1);
        newProject.setId(2L);

        when(projectRepository.existsByUserAndName(testUser, "새로운 프로젝트")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        // When
        var response = projectService.createProject(projectRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("새로운 프로젝트");
        verify(projectRepository).existsByUserAndName(testUser, "새로운 프로젝트");
        verify(projectRepository).save(any(Project.class));
        
        // 이벤트 발행 검증
        ArgumentCaptor<ProjectCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProjectCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ProjectCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getProject()).isNotNull();
        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 이름 중복")
    void createProject_실패_이름_중복() {
        // Given
        when(projectRepository.existsByUserAndName(testUser, "새로운 프로젝트")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(projectRequest, testUser))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 존재하는 프로젝트명입니다.");

        verify(projectRepository).existsByUserAndName(testUser, "새로운 프로젝트");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 - 기본 프로젝트 설정 시 기존 기본 프로젝트 해제")
    void createProject_기본_프로젝트_설정() {
        // Given
        Project existingDefaultProject = ProjectFixture.aProjectFor(testUser);
        existingDefaultProject.setName("기존 기본 프로젝트");
        existingDefaultProject.setIsDefault(true);
        existingDefaultProject.setId(2L);

        ProjectRequest defaultProjectRequest = new ProjectRequest();
        defaultProjectRequest.setName("새로운 기본 프로젝트");
        defaultProjectRequest.setIsDefault(true);

        Project newDefaultProject = ProjectFixture.aProjectFor(testUser);
        newDefaultProject.setName("새로운 기본 프로젝트");
        newDefaultProject.setIsDefault(true);
        newDefaultProject.setPosition(1);
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

        Project updatedProject = ProjectFixture.aProjectFor(testUser);
        updatedProject.setName("수정된 프로젝트");
        updatedProject.setDescription("수정된 설명");
        updatedProject.setColor("#00FF00");
        updatedProject.setIsDefault(false);
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
                .isInstanceOf(BusinessException.class)
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
        Project defaultProject = ProjectFixture.aDefaultProjectFor(testUser);
        defaultProject.setId(1L);

        when(projectRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(defaultProject));

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(1L, testUser))
                .isInstanceOf(BusinessException.class)
                .hasMessage("기본 프로젝트는 삭제할 수 없습니다.");

        verify(projectRepository).findByIdAndUser(1L, testUser);
        verify(todoRepository, never()).updateProjectIdToNullByProjectId(anyLong());
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("기본 프로젝트 조회 성공")
    void getDefaultProject_성공() {
        // Given
        Project defaultProject = ProjectFixture.aDefaultProjectFor(testUser);
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
        Project defaultProject = ProjectFixture.aDefaultProjectFor(testUser);
        defaultProject.setDescription("기본적으로 생성된 프로젝트입니다.");
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
        
        // 이벤트 발행 검증
        ArgumentCaptor<ProjectCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProjectCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ProjectCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getProject()).isNotNull();
        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("기본 프로젝트 생성 실패 - 이미 존재")
    void createDefaultProject_실패_이미_존재() {
        // Given
        Project existingDefaultProject = ProjectFixture.aDefaultProjectFor(testUser);
        existingDefaultProject.setName("기존 기본 프로젝트");
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

