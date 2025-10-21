package swyp.dodream.login.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import swyp.dodream.login.domain.AuthProvider;
import swyp.dodream.login.domain.User;
import swyp.dodream.login.domain.UserRepository;
import swyp.dodream.login.service.TokenService;
import swyp.dodream.login.util.JwtUtil;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtUtil jwtUtil, TokenService tokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // OAuth2 사용자 정보에서 사용자 조회
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub");
        
        User user = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Refresh Token을 Redis에 저장
        tokenService.saveRefreshToken(user.getId(), refreshToken);

        // 응답 헤더에 토큰 추가
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", refreshToken);

        // JSON 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"userId\":%d,\"email\":\"%s\",\"name\":\"%s\"}",
                accessToken, refreshToken, user.getId(), user.getEmail(), user.getName()
        ));
    }
}

