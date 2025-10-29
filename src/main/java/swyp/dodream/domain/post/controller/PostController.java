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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.dto.ApplicationRequest;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.post.dto.PostSummaryResponse;
import swyp.dodream.domain.post.service.PostService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ==============================
    // 홈 목록 조회 (필터 + 검색 + 페이지네이션)
    // ==============================
    @Operation(
            summary = "홈 목록 조회",
            description = "모집글 홈 화면에서 필터(유형, 직군, 기술스택, 관심 분야, 활동방식)와 검색, 페이지네이션으로 모집글을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    })
    @GetMapping("/home")
    public ResponseEntity<Page<PostSummaryResponse>> getHomePosts(
            @Parameter(description = "모집 유형 (ALL, PROJECT, STUDY)", example = "ALL")
            @RequestParam(defaultValue = "ALL") ProjectType type,

            @Parameter(description = "검색 키워드", example = "자바 스터디")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "직군 필터 (예: 백엔드 / 프론트엔드 / 디자이너 등)", example = "백엔드")
            @RequestParam(required = false) String role,

            @Parameter(description = "기술 스택 필터", example = "Spring")
            @RequestParam(required = false) String tech,

            @Parameter(description = "관심 분야 필터", example = "인공지능")
            @RequestParam(required = false) String interest,

            @Parameter(description = "활동 방식 (ONLINE, OFFLINE, HYBRID)", example = "ONLINE")
            @RequestParam(required = false) ActivityMode activityMode,

            @Parameter(description = "모집 중인 글만 보기 여부", example = "true")
            @RequestParam(defaultValue = "true") boolean onlyRecruiting,

            @Parameter(description = "정렬 기준 (latest, popular 등)", example = "latㅓㅇest")
            @RequestParam(defaultValue = "latest") String sort,

            Pageable pageable
    ) {
        Page<PostSummaryResponse> posts = postService.getHomePosts(
                type, keyword, role, tech, interest, activityMode, onlyRecruiting, sort, pageable
        );
        return ResponseEntity.ok(posts);
    }

    // ==============================
    // 모집글 생성
    // ==============================
    @Operation(
            summary = "모집글 생성",
            description = "새로운 프로젝트 또는 스터디 모집글을 작성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
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
    // 모집글 지원
    // ==============================
    @Operation(
            summary = "모집글 지원",
            description = "해당 모집글에 지원합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
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
    // 모집글 삭제
    // ==============================
    @Operation(
            summary = "모집글 삭제",
            description = "작성자가 본인 모집글을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
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

}