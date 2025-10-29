package swyp.dodream.domain.policy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.policy.dto.PolicyResponse;
import swyp.dodream.domain.policy.service.PolicyService;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    // ==============================
    // 정책(이용약관 / 개인정보처리방침) 조회
    // ==============================
    @Operation(
            summary = "정책 조회",
            description = """
                    이용약관 또는 개인정보처리방침을 Markdown 파일로부터 조회합니다.  
                    `type` 파라미터로 TERMS 또는 PRIVACY를 전달해야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정책 조회 성공",
                    content = @Content(schema = @Schema(implementation = PolicyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 정책 타입 요청 (TERMS 또는 PRIVACY만 허용)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/{type}") // /api/policies/TERMS or /api/policies/PRIVACY
    public ResponseEntity<PolicyResponse> getPolicy(
            @Parameter(description = "정책 유형 (TERMS 또는 PRIVACY)", example = "TERMS")
            @PathVariable String type
    ) throws Exception {
        return ResponseEntity.ok(policyService.getPolicy(type));
    }
}
