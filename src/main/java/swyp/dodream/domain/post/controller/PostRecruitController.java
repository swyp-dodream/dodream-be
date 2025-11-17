package swyp.dodream.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.post.dto.response.RecruitApplicationDetailResponse;
import swyp.dodream.domain.post.dto.response.RecruitListResponse;
import swyp.dodream.domain.post.service.RecruitService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostRecruitController {

    private final RecruitService recruitService;

    @Operation(summary = "제안한 내역 조회", description = "리더가 제안한 유저 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/{postId}/recruits/offers")
    public ResponseEntity<RecruitListResponse> getOffers(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        RecruitListResponse response = recruitService.getOffers(
                userPrincipal.getUserId(), postId, cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지원 목록 조회", description = "모집글에 지원한 유저 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/{postId}/recruits/applications")
    public ResponseEntity<RecruitListResponse> getApplications(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        RecruitListResponse response = recruitService.getApplications(
                userPrincipal.getUserId(), postId, cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지원 상세 조회", description = "모집글에 지원한 특정 유저의 상세 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "지원 없음 / 게시글 없음")
    })
    @GetMapping("/{postId}/recruits/applications/{applicationId}")
    public ResponseEntity<RecruitApplicationDetailResponse> getApplicationDetail(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long applicationId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        RecruitApplicationDetailResponse response = recruitService.getApplicationDetail(
                userPrincipal.getUserId(), postId, applicationId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "멤버 내역 조회", description = "수락된 멤버 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/{postId}/recruits/members")
    public ResponseEntity<RecruitListResponse> getMembers(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        RecruitListResponse response = recruitService.getMembers(postId, cursor, size);
        return ResponseEntity.ok(response);
    }
}
