package com.TodoApp.backend.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 응답")
public class MessageResponse {
    @Schema(description = "메시지", example = "작업이 완료되었습니다")
    private String message;
}

