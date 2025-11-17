package swyp.dodream.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 추천 지원자 응답 DTO
 */
@Builder
@Schema(description = "추천 지원자 정보")
public record RecommendedApplicantResponse(
        
        @Schema(description = "지원 ID", example = "123456")
        Long applicationId,
        
        @Schema(description = "지원자 프로필 ID", example = "789")
        Long profileId,
        
        @Schema(description = "지원자 닉네임", example = "개발자김철수")
        String nickname,
        
        @Schema(description = "지원자 프로필 이미지 URL")
        String profileImageUrl,
        
        @Schema(description = "지원 직군", example = "백엔드 개발자")
        String role,
        
        @Schema(description = "경력", example = "3년차")
        String career,
        
        @Schema(description = "지원 메시지", example = "열정적으로 참여하겠습니다!")
        String applicationMessage,
        
        @Schema(description = "유사도 점수", example = "0.92")
        Double similarity,
        
        @Schema(description = "추천 태그 (최대 2개)", example = "[\"나와맞는기술스택\", \"선호하는분야\"]")
        List<String> tags
) {
}

