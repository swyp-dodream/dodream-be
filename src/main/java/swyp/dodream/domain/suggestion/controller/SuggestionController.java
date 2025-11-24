package swyp.dodream.domain.suggestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.suggestion.dto.SuggestionPageResponse;
import swyp.dodream.domain.suggestion.dto.SuggestionRequest;
import swyp.dodream.domain.suggestion.dto.SuggestionResponse;
import swyp.dodream.domain.suggestion.service.SuggestionService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/my/suggestions")
@RequiredArgsConstructor
@Tag(name = "내 제안 내역", description = "내가 제안받은 모집글 관리")
public class SuggestionController {

    private final SuggestionService suggestionService;

    @Operation(
            summary = "내가 제안받은 글 목록 조회",
            description = "리더가 나에게 제안한 모집글 목록을 조회합니다 (페이지네이션)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<SuggestionPageResponse> getMySuggestions(
            Authentication authentication,

            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") int size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        SuggestionPageResponse response = suggestionService.getMySuggestions(
                userPrincipal.getUserId(), page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 제안 보내기", description = "리더가 특정 회원에게 제안을 전송합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "제안 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 유저 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/suggestions")
    public ResponseEntity<SuggestionResponse> sendSuggestion(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody @Valid SuggestionRequest request
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        SuggestionResponse response = suggestionService.createSuggestion(
                userPrincipal.getUserId(), postId, request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "회원 제안 취소", description = "리더가 보낸 제안을 취소합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "제안 내역 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/suggestions/{suggestionId}/cancel")
    public ResponseEntity<Void> cancelSuggestion(
            Authentication authentication,
            @PathVariable Long suggestionId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        suggestionService.cancelSuggestion(suggestionId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "제안 상태 조회", description = "해당 모집글에 대해 특정 사용자에게 보낸 활성 제안이 있는지 확인합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}/suggestions/status")
    public ResponseEntity<Map<String, Object>> getSuggestionStatus(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam Long toUserId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        boolean exists = suggestionService.hasActiveSuggestion(userPrincipal.getUserId(), postId, toUserId);
        Long suggestionId = suggestionService.getActiveSuggestionId(postId, toUserId).orElse(null);
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("suggested", exists);
        body.put("suggestionId", suggestionId);
        return ResponseEntity.ok(body);
    }
}
