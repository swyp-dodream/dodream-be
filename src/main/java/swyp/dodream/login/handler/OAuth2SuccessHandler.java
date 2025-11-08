package swyp.dodream.login.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${frontend.url}")
    private String frontendUrl;

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            log.info("OAuth2 로그인 성공 처리 시작");
            
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                log.error("프론트엔드 URL이 설정되지 않았습니다. frontend.url 프로퍼티를 확인하세요.");
                throw new IllegalStateException("프론트엔드 URL이 설정되지 않았습니다.");
            }
            
            log.info("프론트엔드 URL: {}", frontendUrl);
            
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
                    frontendUrl,
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
}


