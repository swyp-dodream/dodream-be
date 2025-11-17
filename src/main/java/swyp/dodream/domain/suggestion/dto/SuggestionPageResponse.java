package swyp.dodream.domain.suggestion.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SuggestionPageResponse(
        List<SuggestionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static SuggestionPageResponse of(
            List<SuggestionResponse> content,
            int page, int size, long totalElements, int totalPages, boolean hasNext) {
        return SuggestionPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}