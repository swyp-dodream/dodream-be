package swyp.dodream.domain.feedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.feedback.dto.request.FeedbackCreateRequest;
import swyp.dodream.domain.feedback.dto.response.FeedbackCreateResponse;
import swyp.dodream.domain.feedback.dto.response.FeedbackReceivedResponse;
import swyp.dodream.domain.feedback.service.FeedbackService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Tag(name = "피드백", description = "팀원 피드백 작성 및 조회 API")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(
            summary = "피드백 작성",
            description = """
            팀원에게 익명으로 피드백을 작성합니다.
            
            **작성 조건:**
            - 모집마감 후 1달 이후부터 작성 가능
            - 같은 팀원끼리만 작성 가능
            - 본인에게는 작성 불가
            - 한 사람당 1개의 피드백만 작성 가능 (수정/삭제 불가)
            
            **피드백 구성:**
            - 피드백 타입: POSITIVE(좋았어요) 또는 NEGATIVE(아쉬웠어요) 필수 선택
            - 상세 옵션: 최대 3개까지 선택 가능 (긍정/부정 자유롭게 선택 가능)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 작성, 기간 미달, 본인 작성 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 사용자 없음")
    })
    @PostMapping
    public ResponseEntity<FeedbackCreateResponse> createFeedback(
            Authentication authentication,
            @Valid @RequestBody FeedbackCreateRequest request
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        FeedbackCreateResponse response = feedbackService.createFeedback(
                userPrincipal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 받은 피드백 조회",
            description = "내가 받은 모든 피드백을 익명으로 조회합니다. 작성자 정보는 노출되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackReceivedResponse>> getReceivedFeedbacks(
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<FeedbackReceivedResponse> response = feedbackService.getReceivedFeedbacks(
                userPrincipal.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "특정 게시글에서 내가 작성한 피드백 조회",
            description = "특정 프로젝트에서 내가 팀원들에게 작성한 피드백 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/my/{postId}")
    public ResponseEntity<List<FeedbackReceivedResponse>> getMyFeedbacksByPost(
            Authentication authentication,

            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<FeedbackReceivedResponse> response = feedbackService.getMyFeedbacksByPost(
                userPrincipal.getUserId(), postId);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "특정 게시글에서 받은 피드백 조회",
            description = "특정 게시글에서 내가 받은 피드백을 조회합니다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/my/post/{postId}")
    public ResponseEntity<List<FeedbackReceivedResponse>> getReceivedFeedbacksByPost(
            Authentication authentication,

            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") Long postId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<FeedbackReceivedResponse> response = feedbackService.getReceivedFeedbacksByPost(
                userPrincipal.getUserId(), postId);
        return ResponseEntity.ok(response);
    }
}