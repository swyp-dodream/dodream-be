package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record MyPostListResponse(
        List<MyPostResponse> posts,
        long totalCount,
        int totalPages,
        int currentPage,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
    /**
     * Page 객체를 MyPostListResponse로 변환
     *
     * @param page 페이징된 게시글 목록
     * @return MyPostListResponse DTO
     */
    public static MyPostListResponse of(Page<MyPostResponse> page) {
        return MyPostListResponse.builder()
                .posts(page.getContent())
                .totalCount(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}