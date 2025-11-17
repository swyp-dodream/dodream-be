package swyp.dodream.domain.matched.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record MatchedPostPageResponse(
        List<MatchedPostResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static MatchedPostPageResponse of(
            List<MatchedPostResponse> content,
            int page, int size, long totalElements, int totalPages, boolean hasNext) {
        return MatchedPostPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}