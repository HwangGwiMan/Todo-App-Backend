package com.TodoApp.backend.domain.project.mapper;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
import com.TodoApp.backend.domain.project.dto.ProjectResponse;
import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.global.common.mapper.GenericMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProjectMapper extends GenericMapper<ProjectRequest, ProjectResponse, Project> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Project toEntity(ProjectRequest request);

    @Override
    @Mapping(target = "todoCount", ignore = true)
    ProjectResponse toDto(Project project);

    @Mapping(target = "todoCount", source = "todoCount")
    ProjectResponse toDtoWithCount(Project project, Long todoCount);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromDto(ProjectRequest request, @MappingTarget Project project);
}
