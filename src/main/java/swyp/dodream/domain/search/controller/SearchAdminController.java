package swyp.dodream.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.dodream.domain.search.service.PostIndexService;

/**
 * Elasticsearch 인덱스 관리 API
 * 개발/테스트 용도로 사용
 */
@Tag(name = "Search Admin", description = "검색 인덱스 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/search")
public class SearchAdminController {

    private final PostIndexService postIndexService;

    @PostMapping("/reindex")
    @Operation(
            summary = "전체 게시글 재인덱싱",
            description = "DB의 모든 게시글을 Elasticsearch에 다시 인덱싱합니다. 검색이 안 될 때 사용하세요."
    )
    public ResponseEntity<String> reindexAllPosts() {
        postIndexService.reindexAllPosts();
        return ResponseEntity.ok("재인덱싱이 완료되었습니다.");
    }
}