package swyp.dodream.domain.post.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyApplicationPageResponse(
        List<MyApplicationResponse> applications,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static MyApplicationPageResponse of(
            List<MyApplicationResponse> applications,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return MyApplicationPageResponse.builder()
                .applications(applications)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}
