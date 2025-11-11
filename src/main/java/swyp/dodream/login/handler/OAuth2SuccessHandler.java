package swyp.dodream.login.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.jwt.service.TokenService;
import swyp.dodream.jwt.util.JwtUtil;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 기본 프론트엔드 URL (호스트에서 결정 실패 시)
    private static final String DEFAULT_FRONTEND_URL = "https://dodream.store";

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            log.info("OAuth2 로그인 성공 처리 시작");
            
            // 프론트엔드 URL을 요청 호스트 기반으로 결정
            String targetFrontendUrl = resolveFrontendUrl(request);
            log.info("프론트엔드 URL 결정: {}", targetFrontendUrl);
            
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            // CustomOAuth2UserService에서 추가한 사용자 정보 가져오기
            Long userId = oAuth2User.getAttribute("userId");
            String email = oAuth2User.getAttribute("userEmail");
            String name = oAuth2User.getAttribute("userName");
            
            if (userId == null || email == null || name == null) {
                log.error("사용자 정보가 없습니다. userId: {}, email: {}, name: {}", userId, email, name);
                throw ExceptionType.NOT_FOUND_USER.of();
            }
            
            log.info("사용자 정보 - userId: {}, email: {}, name: {}", userId, email, name);

            // JWT 토큰 생성 (userId, email, name 포함)
            String accessToken = jwtUtil.generateAccessToken(userId, email, name);
            String refreshToken = jwtUtil.generateRefreshToken(userId);
            log.info("JWT 토큰 생성 완료");

            // Refresh Token을 Redis에 저장 (이름 포함)
            tokenService.saveRefreshToken(userId, name, refreshToken);
            log.info("Refresh Token 저장 완료");

            // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
            String redirectUrl = String.format(
                    "%s/auth/callback?accessToken=%s&refreshToken=%s&userId=%d&email=%s&name=%s",
                    targetFrontendUrl,
                    accessToken,
                    refreshToken,
                    userId,
                    java.net.URLEncoder.encode(email, "UTF-8"),
                    java.net.URLEncoder.encode(name, "UTF-8")
            );
            
            log.info("프론트엔드로 리다이렉트: {}", redirectUrl.replaceAll("accessToken=[^&]*", "accessToken=***"));
            response.sendRedirect(redirectUrl);
            log.info("OAuth2 로그인 성공 처리 완료");
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류 발생", e);
            // 예외 발생 시 기본 동작 방지 (무한 루프 방지)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"로그인 처리 중 오류가 발생했습니다.\"}");
        }
    }
    

    private String resolveFrontendUrl(HttpServletRequest request) {
        try {
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            String referer = request.getHeader("Referer");
            // dev 환경: dev.dodream.store에서 시작된 흐름은 항상 dev.dodream.store로 리다이렉트
            if ((forwardedHost != null && forwardedHost.contains("dev.dodream.store")) ||
                (referer != null && referer.contains("dev.dodream.store")) ||
                ("dev.dodream.store".equalsIgnoreCase(request.getServerName()))) {
                return "https://dev.dodream.store";
            }
            String host = (forwardedHost != null && !forwardedHost.isBlank())
                    ? forwardedHost
                    : request.getServerName() + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");

            if (host.startsWith("localhost")) {
                return "http://localhost:3000";
            }
            if (host.startsWith("api.")) {
                String base = host.substring(4);
                int colon = base.indexOf(':');
                if (colon > 0) {
                    base = base.substring(0, colon);
                }
                return "https://" + base;
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_FRONTEND_URL;
    }
}


