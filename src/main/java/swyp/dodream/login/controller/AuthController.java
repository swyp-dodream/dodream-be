package swyp.dodream.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.login.dto.TokenResponse;
import swyp.dodream.login.dto.UserResponse;
import swyp.dodream.login.service.AuthService;

import java.util.Map;

@Tag(name = "인증", description = "OAuth2 로그인 및 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse response = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId);

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @Operation(summary = "현재 사용자 정보", description = "JWT로 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = authService.getCurrentUser(userId);

        return ResponseEntity.ok(response);
    }
}
