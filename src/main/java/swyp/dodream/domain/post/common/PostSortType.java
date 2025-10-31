package swyp.dodream.domain.post.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집글 정렬 기준")
public enum PostSortType {
    @Schema(description = "최신순")
    LATEST,

    @Schema(description = "마감순 (가까운 마감일 먼저)")
    DEADLINE,

    @Schema(description = "인기순 (조회수 높은 순)")
    POPULAR
}
