package com.TodoApp.backend.domain.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대시보드 통계 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대시보드 통계 응답")
public class TodoDashboardStatsResponse {

    @Schema(description = "기본 통계")
    private BasicStats basicStats;

    @Schema(description = "상태별 통계")
    private List<StatusStats> statusStats;

    @Schema(description = "우선순위별 통계")
    private List<PriorityStats> priorityStats;

    @Schema(description = "프로젝트별 통계")
    private List<ProjectStats> projectStats;

    /**
     * 기본 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicStats {
        @Schema(description = "전체 개수")
        private Long totalCount;

        @Schema(description = "할 일 개수")
        private Long todoCount;

        @Schema(description = "진행중 개수")
        private Long inProgressCount;

        @Schema(description = "완료 개수")
        private Long doneCount;

        @Schema(description = "지난 마감일 개수")
        private Long overdueCount;

        @Schema(description = "완료율")
        private Double completionRate;
    }

    /**
     * 상태별 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusStats {
        @Schema(description = "상태")
        private String status;

        @Schema(description = "개수")
        private Long count;

        @Schema(description = "비율 (%)")
        private Double percentage;
    }

    /**
     * 우선순위별 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityStats {
        @Schema(description = "우선순위")
        private String priority;

        @Schema(description = "개수")
        private Long count;

        @Schema(description = "비율 (%)")
        private Double percentage;
    }

    /**
     * 프로젝트별 통계
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStats {
        @Schema(description = "프로젝트 ID", types = {"integer", "null"})
        private Long projectId;

        @Schema(description = "프로젝트 이름")
        private String projectName;

        @Schema(description = "프로젝트 색상")
        private String projectColor;

        @Schema(description = "TODO 개수")
        private Long todoCount;

        @Schema(description = "비율 (%)")
        private Double percentage;
    }
}

