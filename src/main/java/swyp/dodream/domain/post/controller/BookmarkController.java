package swyp.dodream.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.post.dto.PostSummaryResponse;
import swyp.dodream.domain.post.service.BookmarkService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // ==============================
    // 북마크 토글 (추가 / 해제)
    // ==============================
    @Operation(
            summary = "북마크 추가 또는 해제",
            description = "특정 모집글에 대해 북마크를 추가하거나 해제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> toggleBookmark(@PathVariable Long postId,
                                                 @AuthenticationPrincipal UserPrincipal user) {
        boolean added = bookmarkService.toggleBookmark(user.getUserId(), postId);
        return ResponseEntity.ok(added ? "북마크 추가됨" : "북마크 해제됨");
    }

    // ==============================
    // 내 북마크 목록 조회
    // ==============================
    @Operation(
            summary = "내 북마크 목록 조회",
            description = "현재 로그인한 사용자의 북마크한 모집글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostSummaryResponse>> getMyBookmarks(@AuthenticationPrincipal UserPrincipal user) {
        List<PostSummaryResponse> bookmarks = bookmarkService.getBookmarkedPosts(user.getUserId());
        return ResponseEntity.ok(bookmarks);
    }
}
