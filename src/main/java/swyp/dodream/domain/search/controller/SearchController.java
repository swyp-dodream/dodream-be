package swyp.dodream.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.search.service.SearchService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    @Operation(
            summary = "게시글 검색",
            description = """
                키워드를 기준으로 게시글 제목/내용을 전체 검색합니다.
                (Elasticsearch 기반)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class)))
    })
    public List<PostResponse> search(
            @Parameter(description = "검색 키워드", example = "자바")
            @RequestParam String keyword
    ) {
        return searchService.searchPosts(keyword);
    }

}
