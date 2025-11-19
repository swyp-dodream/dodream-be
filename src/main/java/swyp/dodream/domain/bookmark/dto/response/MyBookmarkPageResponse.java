package swyp.dodream.domain.bookmark.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyBookmarkPageResponse(
        List<MyBookmarkResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static MyBookmarkPageResponse of(
            List<MyBookmarkResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return MyBookmarkPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}
