package swyp.dodream.login.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.user.domain.OAuthAccount;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.OAuthAccountRepository;
import swyp.dodream.domain.user.repository.UserRepository;
import swyp.dodream.jwt.service.TokenService;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.login.domain.AuthProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // OAuth2 사용자 정보에서 이메일과 프로바이더 추출
        String email;
        AuthProvider provider;
        
        // Google과 Naver의 정보 추출 방식이 다름
        if (oAuth2User.getAttribute("sub") != null) {
            // Google
            email = oAuth2User.getAttribute("email");
            provider = AuthProvider.GOOGLE;
        } else if (oAuth2User.getAttribute("response") != null) {
            // Naver
            java.util.Map<String, Object> naverResponse = oAuth2User.getAttribute("response");
            email = (String) naverResponse.get("email");
            provider = AuthProvider.NAVER;
        } else {
            throw ExceptionType.NOT_FOUND_USER.of();
        }
        
        // OAuth 계정으로 사용자 조회
        OAuthAccount oAuthAccount = oAuthAccountRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());
        
        User user = userRepository.findById(oAuthAccount.getUserId())
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());

        // JWT 토큰 생성 (userId, email, name 포함)
        String accessToken = jwtUtil.generateAccessToken(user.getId(), email, user.getName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Refresh Token을 Redis에 저장 (이름 포함)
        tokenService.saveRefreshToken(user.getId(), user.getName(), refreshToken);

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = String.format(
                "%s/auth/callback?accessToken=%s&refreshToken=%s&userId=%d&email=%s&name=%s",
                frontendUrl,
                accessToken,
                refreshToken,
                user.getId(),
                java.net.URLEncoder.encode(email, "UTF-8"),
                java.net.URLEncoder.encode(user.getName(), "UTF-8")
        );
        
        response.sendRedirect(redirectUrl);
    }
}


