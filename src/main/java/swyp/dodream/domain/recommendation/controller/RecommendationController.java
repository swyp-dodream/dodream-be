package swyp.dodream.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.recommendation.dto.RecommendationListResponse;
import swyp.dodream.domain.recommendation.service.RecommendationService;
import swyp.dodream.jwt.dto.UserPrincipal;

/**
 * 게시글 추천 API 컨트롤러
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "게시글 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "추천 게시글 조회",
            description = "사용자 프로필 기반으로 유사한 게시글을 추천합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<RecommendationListResponse> getRecommendations(
            Authentication authentication,
            @Parameter(description = "다음 페이지 커서")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        RecommendationListResponse response = recommendationService.recommendPosts(
                user.getUserId(), cursor, size
        );
        return ResponseEntity.ok(response);
    }
}

