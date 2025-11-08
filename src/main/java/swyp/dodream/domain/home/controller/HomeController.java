package swyp.dodream.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.home.service.HomeService;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.dto.PostSummaryResponse;

/**
 * 홈 화면 전용 컨트롤러
 * 모집글 필터 + 정렬 + 검색 기능 제공
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "홈 목록 조회",
            description = """
                    홈 화면에서 모집글을 조회합니다.
                    필터(유형, 직군, 기술스택, 관심 분야, 활동방식)와 정렬, 페이지네이션을 지원합니다.
                    직군, 기술스택, 관심 분야는 다중 선택이 가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getHomePosts(
            @Parameter(description = "모집 유형 (ALL, PROJECT, STUDY)", example = "ALL")
            @RequestParam(defaultValue = "ALL") ProjectType type,

            @Parameter(
                    description = "직군 필터 (다중 선택 가능, 예: roles=백엔드&roles=프론트엔드)",
                    example = "[\"백엔드\", \"프론트엔드\"]"
            )
            @RequestParam(required = false) java.util.List<String> roles,

            @Parameter(
                    description = "기술 스택 필터 (다중 선택 가능, 예: techs=Spring&techs=React)",
                    example = "[\"Spring\", \"React\"]"
            )
            @RequestParam(required = false) java.util.List<String> techs,

            @Parameter(
                    description = "관심 분야 필터 (다중 선택 가능, 예: interests=교육&interests=이커머스)",
                    example = "[\"교육\", \"이커머스\"]"
            )
            @RequestParam(required = false) java.util.List<String> interests,

            @Parameter(description = "활동 방식 (ONLINE, OFFLINE, HYBRID)", example = "ONLINE")
            @RequestParam(required = false) ActivityMode activityMode,

            @Parameter(description = "모집 중인 글만 보기 여부", example = "true")
            @RequestParam(defaultValue = "true") boolean onlyRecruiting,

            @Parameter(description = "정렬 기준 (latest, popular, deadline)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort,

            // Pageable을 직접 받지 않고, page와 size를 명시적으로 받습니다.
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        // 서비스에 넘겨줄 Pageable 객체를 여기서 생성합니다.
        Pageable pageable = PageRequest.of(page, size);

        Page<PostSummaryResponse> posts = homeService.getHomePosts(
                type, roles, techs, interests, activityMode, onlyRecruiting, sort, pageable
        );
        return ResponseEntity.ok(posts);
    }
}