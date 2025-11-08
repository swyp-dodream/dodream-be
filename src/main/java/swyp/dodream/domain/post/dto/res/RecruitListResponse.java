package swyp.dodream.domain.post.dto.res;

import lombok.Builder;

import java.util.List;

@Builder
public record RecruitListResponse(
        List<RecruitUserResponse> users,
        Long nextCursor,
        boolean hasNext
) {
}