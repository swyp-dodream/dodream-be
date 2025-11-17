package swyp.dodream.domain.application.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record MyApplicationPageResponse(
        List<MyApplicationResponse> content,  // ✅ applications → content
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static MyApplicationPageResponse of(
            List<MyApplicationResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return MyApplicationPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}