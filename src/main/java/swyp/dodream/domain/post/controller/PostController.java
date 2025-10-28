package swyp.dodream.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()") // 회원 전용
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request,
                                                   @AuthenticationPrincipal UserPrincipal user) {
        PostResponse response = postService.createPost(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal user) {
        PostResponse response = postService.getPostDetail(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> applyToPost(@PathVariable Long postId,
                                            @AuthenticationPrincipal UserPrincipal user,
                                            @RequestBody(required = false) ApplicationRequest request) {
        postService.applyToPost(postId, user.getId(), request);
        return ResponseEntity.ok().build();
    }

}

