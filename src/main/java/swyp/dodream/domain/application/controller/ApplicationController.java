package swyp.dodream.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.application.dto.response.MyApplicationDetailResponse;
import swyp.dodream.domain.application.service.ApplicationService;
import swyp.dodream.domain.application.dto.response.MyApplicationPageResponse;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Tag(name = "내 지원 내역", description = "내가 지원한 모집글 관리")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
            summary = "지원 취소",
            description = """
            수락되기 이전의 본인 지원을 취소합니다.
            - '마이페이지 > 참여 내역 > 지원 내역'에서 취소 버튼 클릭
            - 취소 시 status='WITHDRAWN', withdrawnAt 기록
            (ACTV-04)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 지원은 취소할 수 없음"),
            @ApiResponse(responseCode = "404", description = "지원 내역을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/applications/{applicationId}/cancel")
    public ResponseEntity<Void> cancelMyApplication(
            Authentication authentication,
            @Parameter(name = "applicationId", description = "지원 ID", required = true)
            @PathVariable("applicationId") Long applicationId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        applicationService.cancelByApplicant(applicationId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "내가 지원한 글 목록 조회",
            description = "내가 지원한 모집글 목록을 조회합니다 (페이지네이션)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/applications")
    public ResponseEntity<MyApplicationPageResponse> getMyApplications(
            Authentication authentication,

            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") int size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyApplicationPageResponse response = applicationService.getMyApplications(
                userPrincipal.getUserId(), page, size);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "내 지원 상세 조회",
            description = "내가 지원한 특정 모집글의 상세 정보를 조회합니다 (지원 메시지, 직군 등)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "지원 정보 없음")
    })
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<MyApplicationDetailResponse> getMyApplicationDetail(
            Authentication authentication,

            @Parameter(name = "applicationId", description = "지원 ID", required = true)
            @PathVariable("applicationId") Long applicationId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyApplicationDetailResponse response = applicationService.getMyApplicationDetail(
                userPrincipal.getUserId(), applicationId);
        return ResponseEntity.ok(response);
    }
}
