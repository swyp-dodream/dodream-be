package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Slice;

import java.util.List;

@Builder
public record RecruitListResponse(
        List<RecruitUserResponse> users,
        Long nextCursor,
        boolean hasNext
) {
    public static RecruitListResponse of(Slice<RecruitUserResponse> slice) {
        List<RecruitUserResponse> users = slice.getContent();

        Long nextCursor = users.isEmpty() ? null :
                users.get(users.size() - 1).userId();

        return RecruitListResponse.builder()
                .users(users)
                .nextCursor(nextCursor)
                .hasNext(slice.hasNext())
                .build();
    }
}