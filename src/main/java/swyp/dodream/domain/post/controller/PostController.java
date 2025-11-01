package swyp.dodream.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.application.dto.ApplicationRequest;
import swyp.dodream.domain.application.dto.CanApplyResponse;
import swyp.dodream.domain.post.common.PostSortType;
import swyp.dodream.domain.post.dto.*;
import swyp.dodream.domain.post.service.PostService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ==============================
    // 모집글 생성
    // ==============================
    @Operation(
            summary = "모집글 생성",
            description = "새로운 프로젝트 또는 스터디 모집글을 작성합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "모집글 생성 요청 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PostCreateRequest.class))
            )
            @RequestBody @Valid PostCreateRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        PostResponse response = postService.createPost(request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==============================
    // 모집글 상세 조회
    // ==============================
    @Operation(
            summary = "모집글 상세 조회",
            description = "특정 모집글의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "모집글을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(
            @Parameter(description = "모집글 ID", example = "123")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        PostResponse response = postService.getPostDetail(id, user.getUserId());
        return ResponseEntity.ok(response);
    }

    // ==============================
    // 모집글 목록 조회 (정렬)
    // ==============================
    @Operation(
            summary = "모집글 목록 조회",
            description = "정렬 기준(최신순, 마감순, 인기순)에 따라 모집글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @Parameter(description = "정렬 기준 (LATEST, DEADLINE, POPULAR)", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PostSortType sortType,

            Pageable pageable
    ) {
        Page<PostResponse> posts = postService.getPosts(sortType, pageable);
        return ResponseEntity.ok(posts);
    }


    // ==============================
    // 모집글 수정
    // ==============================
    @Operation(
            summary = "모집글 수정",
            description = "작성자가 본인 모집글의 내용을 수정합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "모집글을 찾을 수 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "모집글 ID", example = "123")
            @PathVariable Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 모집글 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PostCreateRequest.class))
            )
            @RequestBody @Valid PostUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        PostResponse updated = postService.updatePost(postId, request, user.getUserId());
        return ResponseEntity.ok(updated);
    }

    // ==============================
    // 모집글 삭제
    // ==============================
    @Operation(
            summary = "모집글 삭제",
            description = "작성자가 본인 모집글을 삭제합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "모집글을 찾을 수 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "모집글 ID", example = "123")
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        postService.deletePost(postId, user.getUserId());
        return ResponseEntity.noContent().build(); // 204 응답
    }

    // ==============================
    // 모집글 지원
    // ==============================
    @Operation(
            summary = "모집글 지원",
            description = "해당 모집글에 지원합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 성공"),
            @ApiResponse(responseCode = "400", description = "이미 지원했거나 조건 불일치"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/apply")
    public ResponseEntity<Void> applyToPost(
            @Parameter(description = "모집글 ID", example = "123")
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "지원 시 첨부 데이터",
                    required = false,
                    content = @Content(schema = @Schema(implementation = ApplicationRequest.class))
            )
            @RequestBody(required = false) ApplicationRequest request
    ) {
        postService.applyToPost(postId, user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    // ==============================
    // 모집글 지원 가능 여부 조회
    // ==============================
    @Operation(
            summary = "모집글 지원 가능 여부 조회",
            description = "현재 로그인한 사용자가 특정 모집글에 지원할 수 있는지 여부를 반환합니다. "
                    + "모집글 작성자(리더)는 false를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CanApplyResponse.class))),
            @ApiResponse(responseCode = "404", description = "모집글을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/{postId}/can-apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CanApplyResponse> canApplyToPost(
            @Parameter(description = "모집글 ID", example = "123")
            @PathVariable Long postId,

            @AuthenticationPrincipal UserPrincipal user,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "지원 시 첨부 데이터",
                    required = false,
                    content = @Content(schema = @Schema(implementation = ApplicationRequest.class))
            )
            @RequestBody(required = false) ApplicationRequest request
    ) {
        boolean canApply = postService.canApply(postId, user.getUserId());
        return ResponseEntity.ok(new CanApplyResponse(canApply));
    }

    /**
     * 내가 쓴 글 목록 조회
     * <p>
     * GET /api/posts/my?tab=all&status=recruiting&page=0&size=10
     *
     * @param tab    탭 필터: "project" (프로젝트), "study" (스터디)
     * @param status 모집 상태: "recruiting" (모집 중), "completed" (모집 완료), null (전체)
     * @param page   페이지 번호 (0부터 시작, 기본값: 0)
     * @param size   페이지 크기 (기본값: 10)
     */
    @Operation(summary = "내가 쓴 글 조회")
    @GetMapping("/my")
    public ResponseEntity<MyPostListResponse> getMyPosts(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "project") String tab,
            @RequestParam(required = false) String status, // null 가능함!
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyPostListResponse response = postService.getMyPosts(
                userPrincipal.getUserId(), tab, status, page, size
        );
        return ResponseEntity.ok(response);
    }
}