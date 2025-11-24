package com.TodoApp.backend.domain.todo.repository;

import com.TodoApp.backend.domain.todo.dto.TodoDashboardStatsResponse;

import java.util.List;

public interface TodoRepositoryCustom {
    List<TodoDashboardStatsResponse.StatusStats> findStatusStatsByUserId(Long userId, long totalCount);
    List<TodoDashboardStatsResponse.PriorityStats> findPriorityStatsByUserId(Long userId, long totalCount);
    List<TodoDashboardStatsResponse.ProjectStats> findProjectStatsByUserId(Long userId, long totalCount);
    Long countNoProjectTodosByUserId(Long userId);
}
