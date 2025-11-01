package swyp.dodream.domain.recommendation.dto;

import lombok.Builder;

import java.util.List;

/**
 * 추천 게시글 목록 응답 DTO
 */
@Builder
public record RecommendationListResponse(
        List<RecommendationPostResponse> posts,
        Long nextCursor,
        boolean hasNext
) {
    /**
     * 빌더로 직접 생성
     */
    public static RecommendationListResponse of(
            List<RecommendationPostResponse> posts,
            Long nextCursor,
            boolean hasNext
    ) {
        return RecommendationListResponse.builder()
                .posts(posts)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}

