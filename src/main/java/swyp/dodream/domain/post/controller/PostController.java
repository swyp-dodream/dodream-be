package swyp.dodream.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.dto.ApplicationRequest;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import org.springframework.http.ResponseEntity;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.post.dto.PostSummaryResponse;
import swyp.dodream.domain.post.service.PostService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 홈 목록 조회 (필터 + 검색 + 페이지네이션)
    @GetMapping("/home")
    public ResponseEntity<Page<PostSummaryResponse>> getHomePosts(
            @RequestParam(defaultValue = "ALL") ProjectType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String tech,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false) ActivityMode activityMode,
            @RequestParam(defaultValue = "true") boolean onlyRecruiting,
            @RequestParam(defaultValue = "latest") String sort,
            Pageable pageable
    ) {
        Page<PostSummaryResponse> posts = postService.getHomePosts(
                type, keyword, role, tech, interest, activityMode, onlyRecruiting, sort, pageable
        );
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // 회원 전용

    // 모집글 생성
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request,
                                                   @AuthenticationPrincipal UserPrincipal user) {
        PostResponse response = postService.createPost(request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 모집글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        PostResponse response = postService.getPostDetail(id, user.getUserId());
        return ResponseEntity.ok(response);
    }

    // 모집글 지원
    @PostMapping("/{postId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> applyToPost(@PathVariable Long postId,
                                            @AuthenticationPrincipal UserPrincipal user,
                                            @RequestBody(required = false) ApplicationRequest request) {
        postService.applyToPost(postId, user.getUserId(), request);
        return ResponseEntity.ok().build();
    }
}

