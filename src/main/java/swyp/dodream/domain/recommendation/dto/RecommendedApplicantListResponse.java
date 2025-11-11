package swyp.dodream.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 추천 지원자 목록 응답 DTO
 */
@Builder
@Schema(description = "추천 지원자 목록")
public record RecommendedApplicantListResponse(
        
        @Schema(description = "추천된 지원자 목록 (최대 3명)")
        List<RecommendedApplicantResponse> applicants,
        
        @Schema(description = "총 추천 수", example = "3")
        Integer totalCount
) {
}

