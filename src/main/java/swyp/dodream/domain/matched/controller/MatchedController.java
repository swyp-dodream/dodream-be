package swyp.dodream.domain.matched.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.matched.dto.MatchingCancelRequest;
import swyp.dodream.domain.matched.service.MatchedService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/matches")
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
}
