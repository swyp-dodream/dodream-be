package swyp.dodream.domain.url.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.url.dto.ProfileUrlCreateRequest;
import swyp.dodream.domain.url.dto.ProfileUrlResponse;
import swyp.dodream.domain.url.dto.ProfileUrlUpdateRequest;
import swyp.dodream.domain.url.service.ProfileUrlService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/urls")
@RequiredArgsConstructor
@Tag(name = "Profile URL", description = "프로필 URL 관리 API")
public class ProfileUrlController {

    private final ProfileUrlService profileUrlService;

    @Operation(summary = "URL 추가", description = "프로필에 새로운 URL을 추가합니다")
    @PostMapping
    public ResponseEntity<ProfileUrlResponse> addUrl(
            Authentication authentication,
            @Valid @RequestBody ProfileUrlCreateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        ProfileUrlResponse response = profileUrlService.addUrl(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "URL 목록 조회", description = "사용자의 모든 URL을 조회합니다")
    @GetMapping
    public ResponseEntity<List<ProfileUrlResponse>> getUrls(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        List<ProfileUrlResponse> response = profileUrlService.getUrls(userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "URL 수정", description = "기존 URL을 수정합니다")
    @PutMapping("/{urlId}")
    public ResponseEntity<ProfileUrlResponse> updateUrl(
            Authentication authentication,
            @PathVariable Long urlId,
            @Valid @RequestBody ProfileUrlUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        ProfileUrlResponse response = profileUrlService.updateUrl(userId, urlId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "URL 삭제", description = "URL을 삭제합니다")
    @DeleteMapping("/{urlId}")
    public ResponseEntity<String> deleteUrl(
            Authentication authentication,
            @PathVariable Long urlId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        profileUrlService.deleteUrl(userId, urlId);
        return ResponseEntity.ok("URL이 삭제되었습니다");
    }
}