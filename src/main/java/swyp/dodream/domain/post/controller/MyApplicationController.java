package swyp.dodream.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.post.dto.res.MyApplicationListResponse;
import swyp.dodream.domain.post.service.MyApplicationService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Tag(name = "내 신청 내역", description = "일반 유저의 지원/제안/매칭 내역 조회")
public class MyApplicationController {

    private final MyApplicationService myApplicationService;

    @Operation(
            summary = "내가 지원한 글 조회",
            description = "내가 지원한 모집글 목록을 조회합니다 (무한 스크롤)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/applications")
    public ResponseEntity<MyApplicationListResponse> getMyApplications(
            Authentication authentication,

            @Parameter(name = "cursor", description = "다음 페이지 커서")
            @RequestParam(required = false) Long cursor,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyApplicationListResponse response = myApplicationService.getMyApplications(
                userPrincipal.getUserId(), cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 제안받은 글 조회",
            description = "리더가 나에게 제안한 모집글 목록을 조회합니다 (무한 스크롤)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/suggestions")
    public ResponseEntity<MyApplicationListResponse> getMySuggestions(
            Authentication authentication,

            @Parameter(name = "cursor", description = "다음 페이지 커서")
            @RequestParam(required = false) Long cursor,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyApplicationListResponse response = myApplicationService.getMySuggestions(
                userPrincipal.getUserId(), cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 매칭된 글 조회",
            description = "내가 수락되어 참여 중인 모집글 목록을 조회합니다 (무한 스크롤)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/matched")
    public ResponseEntity<MyApplicationListResponse> getMyMatched(
            Authentication authentication,

            @Parameter(name = "cursor", description = "다음 페이지 커서")
            @RequestParam(required = false) Long cursor,

            @Parameter(name = "size", description = "페이지 크기 (기본 10개)")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        MyApplicationListResponse response = myApplicationService.getMyMatched(
                userPrincipal.getUserId(), cursor, size);
        return ResponseEntity.ok(response);
    }
}
