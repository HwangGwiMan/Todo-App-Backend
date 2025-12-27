package com.TodoApp.backend.domain.todo.mapper;

import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.global.common.mapper.GenericMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TodoMapper extends GenericMapper<TodoRequest, TodoResponse, Todo> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Todo toEntity(TodoRequest request);

    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    TodoResponse toDto(Todo todo);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromDto(TodoRequest request, @MappingTarget Todo todo);
}
