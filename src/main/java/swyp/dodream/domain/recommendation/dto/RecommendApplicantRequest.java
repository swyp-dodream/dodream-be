package swyp.dodream.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 지원 유저 AI 추천 요청 DTO
 */
@Schema(description = "지원 유저 AI 추천 요청")
public record RecommendApplicantRequest(
        
        @Schema(description = "추천받을 직군 ID", example = "1")
        @NotNull(message = "직군 ID는 필수입니다")
        Long roleId
) {
}

