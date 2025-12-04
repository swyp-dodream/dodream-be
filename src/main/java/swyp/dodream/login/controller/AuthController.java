package swyp.dodream.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.user.dto.UserResponse;
import swyp.dodream.jwt.dto.UserPrincipal;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.login.dto.TokenResponse;
import swyp.dodream.login.service.AuthService;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "인증", description = "OAuth2 로그인 및 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response) {
        
        // Refresh Token을 헤더 또는 쿠키에서 가져오기
        String refreshToken = refreshTokenHeader != null ? refreshTokenHeader : refreshTokenCookie;
        
        if (refreshToken == null) {
            throw ExceptionType.UNAUTHORIZED_TOKEN_INVALID.of();
        }
        
        TokenResponse tokenResponse = authService.reissueToken(refreshToken);
        
        // 새로운 Access Token을 쿠키에 저장 (설정 파일의 만료 시간 사용)
        Cookie accessTokenCookie = createCookie("accessToken", tokenResponse.getAccessToken(), jwtUtil.getAccessTokenExpirationInSeconds(), request.isSecure());
        response.addCookie(accessTokenCookie);
        
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response) {
        if (authentication == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        authService.logout(userPrincipal.getUserId(), userPrincipal.getName());

        // 쿠키 삭제
        deleteCookie("accessToken", response, request.isSecure());
        deleteCookie("refreshToken", response, request.isSecure());

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @Operation(summary = "현재 사용자 정보", description = "JWT로 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserResponse response = authService.getCurrentUser(userPrincipal.getUserId());

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "OAuth2 로그인 시작", description = "프론트엔드 URL을 쿠키에 저장하고 OAuth2 로그인 페이지로 리다이렉트합니다.")
    @GetMapping("/oauth2/authorize/{provider}")
    public void startOAuth2Login(
            @PathVariable String provider,
            @RequestParam(required = false) String frontend_url,
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        // 프론트엔드 URL이 있으면 쿠키에 저장
        if (frontend_url != null && !frontend_url.trim().isEmpty()) {
            try {
                String decodedUrl = URLDecoder.decode(frontend_url, StandardCharsets.UTF_8);
                Cookie cookie = new Cookie("OAUTH2_FRONTEND_URL", decodedUrl);
                cookie.setPath("/");
                cookie.setMaxAge(300); // 5분
                cookie.setHttpOnly(true);
                cookie.setSecure(request.isSecure()); // HTTPS인 경우에만 secure
                response.addCookie(cookie);
            } catch (Exception e) {
                // 쿠키 설정 실패해도 계속 진행
            }
        }
        
        // OAuth2 로그인 페이지로 리다이렉트
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    // 쿠키 생성 헬퍼 메서드
    private Cookie createCookie(String name, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    // 쿠키 삭제 헬퍼 메서드
    private void deleteCookie(String name, HttpServletResponse response, boolean secure) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}
