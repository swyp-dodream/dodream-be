package swyp.dodream.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.login.domain.User;
import swyp.dodream.login.domain.UserRepository;
import swyp.dodream.login.service.TokenService;
import swyp.dodream.login.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증", description = "OAuth2 로그인 및 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AuthController(JwtUtil jwtUtil, TokenService tokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            // Refresh Token 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "유효하지 않은 Refresh Token입니다."));
            }

            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            // Redis에 저장된 Refresh Token과 비교
            if (!tokenService.validateRefreshToken(userId, refreshToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "만료되었거나 유효하지 않은 토큰입니다."));
            }

            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 새로운 Access Token 발급
            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("message", "토큰 재발급 성공");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "토큰 재발급 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증되지 않은 사용자입니다."));
        }

        Long userId = (Long) authentication.getPrincipal();
        
        // Redis에서 Refresh Token 삭제
        tokenService.deleteRefreshToken(userId);

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @Operation(summary = "현재 사용자 정보", description = "JWT로 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증되지 않은 사용자입니다."));
        }

        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("profileImageUrl", user.getProfileImageUrl());
        response.put("provider", user.getProvider());

        return ResponseEntity.ok(response);
    }
}


