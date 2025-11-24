package com.TodoApp.backend.domain.todo.repository;

import com.TodoApp.backend.domain.todo.dto.TodoDashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final DSLContext dslContext;

    @Override
    public List<TodoDashboardStatsResponse.StatusStats> findStatusStatsByUserId(Long userId, long totalCount) {
        var result = dslContext
                .select(
                        DSL.field("status", String.class).as("status"),
                        DSL.count().as("count")
                )
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .groupBy(DSL.field("status"))
                .fetch();

        return result.stream()
                .map(record -> {
                    String status = record.get("status", String.class);
                    Long count = record.get("count", Long.class);
                    long countValue = count != null ? count : 0L;
                    double percentage = totalCount > 0 ? (double) countValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.StatusStats.builder()
                            .status(status != null ? status : "")
                            .count(countValue)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoDashboardStatsResponse.PriorityStats> findPriorityStatsByUserId(Long userId, long totalCount) {
        var result = dslContext
                .select(
                        DSL.field("priority", String.class).as("priority"),
                        DSL.count().as("count")
                )
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .and(DSL.field("priority").isNotNull())
                .groupBy(DSL.field("priority"))
                .fetch();

        return result.stream()
                .map(record -> {
                    String priority = record.get("priority", String.class);
                    Long count = record.get("count", Long.class);
                    long countValue = count != null ? count : 0L;
                    double percentage = totalCount > 0 ? (double) countValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.PriorityStats.builder()
                            .priority(priority != null ? priority : "")
                            .count(countValue)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoDashboardStatsResponse.ProjectStats> findProjectStatsByUserId(Long userId, long totalCount) {
        // 프로젝트별 통계 (프로젝트가 있는 TODO)
        var result = dslContext
                .select(
                        DSL.field("t.project_id").as("project_id"),
                        DSL.field("p.name").as("project_name"),
                        DSL.field("p.color").as("project_color"),
                        DSL.count().as("todo_count")
                )
                .from(DSL.table("todos").as("t"))
                .leftJoin(DSL.table("projects").as("p"))
                .on(DSL.field("t.project_id").eq(DSL.field("p.id")))
                .where(DSL.field("t.user_id").eq(userId))
                .and(DSL.field("t.project_id").isNotNull())
                .groupBy(
                        DSL.field("t.project_id"),
                        DSL.field("p.name"),
                        DSL.field("p.color")
                )
                .orderBy(DSL.count().desc())
                .fetch();

        List<TodoDashboardStatsResponse.ProjectStats> projectStats = result.stream()
                .map(record -> {
                    Long projectId = record.get("project_id", Long.class);
                    String projectName = record.get("project_name", String.class);
                    String projectColor = record.get("project_color", String.class);
                    Long todoCount = record.get("todo_count", Long.class);
                    long todoCountValue = todoCount != null ? todoCount : 0L;
                    double percentage = totalCount > 0 ? (double) todoCountValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.ProjectStats.builder()
                            .projectId(projectId)
                            .projectName(projectName != null ? projectName : "알 수 없음")
                            .projectColor(projectColor != null ? projectColor : "#9CA3AF")
                            .todoCount(todoCountValue)
                            .percentage(percentage)
                            .build();
                }).collect(Collectors.toList());

        // 프로젝트가 없는 TODO 통계
        Long noProjectCount = countNoProjectTodosByUserId(userId);
        if (noProjectCount != null && noProjectCount > 0) {
            double percentage = totalCount > 0 ? (double) noProjectCount / totalCount * 100 : 0.0;
            projectStats.add(TodoDashboardStatsResponse.ProjectStats.builder()
                    .projectId(null)
                    .projectName("프로젝트 없음")
                    .projectColor("#9CA3AF")
                    .todoCount(noProjectCount)
                    .percentage(percentage)
                    .build());
        }

        return projectStats;
    }

    @Override
    public Long countNoProjectTodosByUserId(Long userId) {
        return dslContext
                .selectCount()
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .and(DSL.field("project_id").isNull())
                .fetchOne(0, Long.class);
    }
}
