package swyp.dodream.domain.matched.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.matched.dto.MatchedPostPageResponse;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.matched.dto.MatchingCancelRequest;
import swyp.dodream.domain.matched.service.MatchedService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/matched")
@RequiredArgsConstructor
@Tag(name = "Match", description = "매칭 관련 API")
public class MatchedController {

    private final MatchedService matchedService;

    @Operation(
            summary = "매칭 취소",
            description = """
                            리더 또는 멤버가 매칭을 취소합니다.
                            - 리더: 해당 모집글 단위로 최대 2회 취소 가능
                            - 멤버: 매칭 24시간 이내 무제한, 이후에는 월 2회 제한
                            - 상호 알림 발송
                            (ACTV-05)
                           """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "취소 한도 초과"),
            @ApiResponse(responseCode = "404", description = "매칭 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable Long matchingId,
            @RequestBody(required = false) MatchingCancelRequest request) {

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        matchedService.cancelMatching(matchingId, user.getUserId(),
                request == null ? new MatchingCancelRequest(CancelReasonCode.OTHER, null) : request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "내가 매칭된 글 목록 조회",
            description = "내가 수락되어 참여 중인 모집글 목록을 조회합니다 (페이지네이션)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<MatchedPostPageResponse> getMyMatched(
            Authentication authentication,

            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") int size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MatchedPostPageResponse response = matchedService.getMyMatched(
                userPrincipal.getUserId(), page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지원 수락", description = "리더가 특정 지원자를 수락하여 매칭을 생성합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "매칭 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 지원자 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/applications/{applicationId}/accept")
    public ResponseEntity<Void> acceptApplication(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long applicationId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        matchedService.acceptApplication(userPrincipal.getUserId(), postId, applicationId);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "제안 수락", description = "유저가 리더의 제안을 수락하여 매칭을 생성합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "매칭 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "제안 내역 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/suggestions/{suggestionId}/accept")
    public ResponseEntity<Void> acceptSuggestion(
            Authentication authentication,
            @PathVariable Long suggestionId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        matchedService.acceptSuggestion(userPrincipal.getUserId(), suggestionId);
        return ResponseEntity.status(201).build();
    }
}
