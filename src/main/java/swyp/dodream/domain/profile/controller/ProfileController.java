package swyp.dodream.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.profile.dto.ProfileCreateRequest;
import swyp.dodream.domain.profile.dto.ProfileResponse;
import swyp.dodream.domain.profile.dto.ProfileUpdateRequest;
import swyp.dodream.domain.profile.service.ProfileService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileController {
    
    private final ProfileService profileService;
    
    @Operation(summary = "프로필 생성", description = "온보딩을 통해 프로필을 생성합니다")
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            Authentication authentication,
            @RequestBody ProfileCreateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "프로필 조회", description = "현재 사용자의 프로필을 조회합니다")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "프로필 수정", description = "프로필 정보를 수정합니다")
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody ProfileUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "프로필 삭제", description = "프로필을 삭제합니다")
    @DeleteMapping
    public ResponseEntity<String> deleteProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        profileService.deleteProfile(userId);
        return ResponseEntity.ok("프로필이 삭제되었습니다");
    }
}
