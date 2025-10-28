package swyp.dodream.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.post.service.PostService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

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

