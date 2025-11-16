package swyp.dodream.domain.post.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyApplicationListResponse(
        List<MyApplicationResponse> applications,
        Long nextCursor,
        boolean hasNext
) {
    /**
     * 빌더로 직접 생성
     */
    public static MyApplicationListResponse of(
            List<MyApplicationResponse> applications,
            Long nextCursor,
            boolean hasNext
    ) {
        return MyApplicationListResponse.builder()
                .applications(applications)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}