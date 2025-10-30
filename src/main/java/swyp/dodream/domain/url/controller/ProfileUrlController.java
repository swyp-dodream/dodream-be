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

//    private final ProfileUrlService profileUrlService;
//
//    @Operation(summary = "URL 삭제", description = "URL을 삭제합니다")
//    @DeleteMapping("/{urlId}")
//    public ResponseEntity<String> deleteUrl(
//            Authentication authentication,
//            @PathVariable Long urlId) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        Long userId = userPrincipal.getUserId();
//
//        profileUrlService.deleteUrl(userId, urlId);
//        return ResponseEntity.ok("URL이 삭제되었습니다");
//    }
}