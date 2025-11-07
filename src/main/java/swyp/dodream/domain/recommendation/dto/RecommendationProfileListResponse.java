package swyp.dodream.domain.recommendation.dto;

import lombok.Builder;

import java.util.List;

/**
 * 추천 프로필 목록 응답 DTO
 */
@Builder
public record RecommendationProfileListResponse(
        List<RecommendationProfileResponse> profiles,
        Long nextCursor,
        boolean hasNext
) {
    /**
     * 빌더로 직접 생성
     */
    public static RecommendationProfileListResponse of(
            List<RecommendationProfileResponse> profiles,
            Long nextCursor,
            boolean hasNext
    ) {
        return RecommendationProfileListResponse.builder()
                .profiles(profiles)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}

