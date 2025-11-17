package swyp.dodream.domain.suggestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.suggestion.dto.SuggestionPageResponse;
import swyp.dodream.domain.suggestion.service.SuggestionService;
import swyp.dodream.jwt.dto.UserPrincipal;

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
}
