package swyp.dodream.domain.profile.controller;

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
import swyp.dodream.domain.ai.dto.IntroAiDraftRequest;
import swyp.dodream.domain.ai.service.AiDraftService;
import swyp.dodream.domain.profile.dto.request.AccountSettingsUpdateRequest;
import swyp.dodream.domain.profile.dto.request.ProfileCreateRequest;
import swyp.dodream.domain.profile.dto.request.ProfileMyPageUpdateRequest;
import swyp.dodream.domain.profile.dto.response.AccountSettingsResponse;
import swyp.dodream.domain.profile.dto.response.ProfileMyPageResponse;
import swyp.dodream.domain.profile.dto.response.ProfileResponse;
import swyp.dodream.domain.profile.service.ProfileService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관련 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "초기 프로필 생성", description = "온보딩을 통해 프로필을 생성합니다")
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileCreateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "프로필 삭제", description = "프로필을 삭제합니다")
//    @DeleteMapping
//    public ResponseEntity<String> deleteProfile(Authentication authentication) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        profileService.deleteProfile(userId);
//        return ResponseEntity.ok("프로필이 삭제되었습니다");
//    }

//    // === 관심 분야 관리 ===
//    @PostMapping("/interestKeywords")
//    @Operation(summary = "관심 분야 추가", description = "프로필에 관심 분야를 여러 개 추가합니다 (이름으로 요청)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "관심 분야 추가 성공"),
//            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
//            @ApiResponse(responseCode = "409", description = "이미 추가된 관심 분야")
//    })
//    public ResponseEntity<List<ProfileInterestKeywordResponse>> addInterestKeywords(
//            Authentication authentication,
//            @RequestParam List<String> interestKeywordNames) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        List<ProfileInterestKeywordResponse> response = profileService.addInterestKeywords(userId, interestKeywordNames);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @DeleteMapping("/interestKeywords")
//    @Operation(summary = "관심 분야 삭제", description = "프로필에서 관심 분야를 삭제합니다 (이름으로 요청)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "204", description = "관심 분야 삭제 성공"),
//            @ApiResponse(responseCode = "404", description = "관심 분야를 찾을 수 없음")
//    })
//    public ResponseEntity<Void> removeInterestKeyword(
//            Authentication authentication,
//            @RequestParam String interestKeywordName) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        profileService.removeInterestKeyword(userId, interestKeywordName);
//        return ResponseEntity.noContent().build();
//    }
//
//    // === 기술 스택 관리 ===
//    @PostMapping("/techStacks")
//    @Operation(summary = "기술 스택 추가", description = "프로필에 기술 스택을 여러 개 추가합니다 (이름으로 요청)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "기술 스택 추가 성공"),
//            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
//            @ApiResponse(responseCode = "409", description = "이미 추가된 기술 스택")
//    })
//    public ResponseEntity<List<ProfileTechStackResponse>> addTechStacks(
//            Authentication authentication,
//            @RequestParam List<String> techSkillNames) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        List<ProfileTechStackResponse> response = profileService.addTechStacks(userId, techSkillNames);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @DeleteMapping("/techStacks")
//    @Operation(summary = "기술 스택 삭제", description = "프로필에서 기술 스택을 삭제합니다 (이름으로 요청)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "204", description = "기술 스택 삭제 성공"),
//            @ApiResponse(responseCode = "404", description = "기술 스택을 찾을 수 없음")
//    })
//    public ResponseEntity<Void> removeTechStack(
//            Authentication authentication,
//            @RequestParam String techSkillName) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        profileService.removeTechStack(userId, techSkillName);
//        return ResponseEntity.noContent().build();
//    }
//
    // === 내 프로필 관리 ===
    @Operation(summary = "내 프로필 조회", description = "내 프로필 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileMyPageResponse> getMyProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        ProfileMyPageResponse response = profileService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 수정", description = "내 프로필 정보를 수정합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 프로필 수정 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임")
    })
    @PutMapping("/me")
    public ResponseEntity<ProfileMyPageResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileMyPageUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        ProfileMyPageResponse response = profileService.updateMyProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    // === 계정설정 관리 ===
    @Operation(summary = "내 계정 설정 조회", description = "내 계정 설정 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계정설정 조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필 또는 사용자 정보를 찾을 수 없음")
    })
    @GetMapping("/settings")
    public ResponseEntity<AccountSettingsResponse> getAccountSettings(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        AccountSettingsResponse response = profileService.getAccountSettingsWithEmail(userId, userPrincipal.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 계정 설정 수정", description = "내 계정 설정 정보를 수정합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계정 설정 수정 성공"),
            @ApiResponse(responseCode = "404", description = "프로필 또는 사용자 정보를 찾을 수 없음")
    })
    @PutMapping("/settings")
    public ResponseEntity<AccountSettingsResponse> updateAccountSettings(
            Authentication authentication,
            @Valid @RequestBody AccountSettingsUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        AccountSettingsResponse response = profileService.updateAccountSettings(userId, userPrincipal.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    private final AiDraftService aiDraftService;

    @Operation(summary = "AI 자기소개 초안 생성", description = "프로필 데이터 기반으로 200자 이내 자기소개 초안을 생성합니다.")
    @PostMapping("/intro/ai-draft")
    public ResponseEntity<String> generateIntro(@Valid @RequestBody IntroAiDraftRequest request) {
        String draft = aiDraftService.createIntroDraft(null, request);
        return ResponseEntity.ok(draft);
    }

    @Operation(
            summary = "지원자 프로필 조회",
            description = "모집글 작성자가 지원자의 프로필을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아니거나 지원자가 아님)"),
            @ApiResponse(responseCode = "404", description = "프로필 또는 지원서를 찾을 수 없음")
    })
    @GetMapping("/applicant/{userId}/post/{postId}")
    public ResponseEntity<ProfileMyPageResponse> getApplicantProfile(
            Authentication authentication,

            @Parameter(description = "조회할 지원자 ID", required = true)
            @PathVariable Long userId,

            @Parameter(description = "모집글 ID", required = true)
            @PathVariable Long postId
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long requesterId = userPrincipal.getUserId();

        ProfileMyPageResponse response = profileService.getApplicantProfile(
                requesterId, userId, postId);
        return ResponseEntity.ok(response);
    }
}