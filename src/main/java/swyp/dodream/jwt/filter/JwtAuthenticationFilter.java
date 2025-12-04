package swyp.dodream.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import swyp.dodream.jwt.dto.UserPrincipal;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.login.dto.TokenResponse;
import swyp.dodream.login.service.AuthService;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 재발급 엔드포인트는 자동 재발급 로직 제외 (무한 루프 방지)
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.contains("/api/auth/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 또는 쿠키에서 Access Token 추출
        String token = resolveToken(request);

        if (token != null) {
            // Access Token이 유효한 경우
            if (jwtUtil.validateToken(token)) {
                setAuthentication(token);
            } 
            // Access Token이 만료된 경우 자동 재발급 시도
            else if (jwtUtil.isTokenExpired(token)) {
                log.info("Access Token 만료 감지, 자동 재발급 시도");
                
                String refreshToken = getRefreshTokenFromCookie(request);
                if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                    try {
                        // Refresh Token으로 새 Access Token 발급
                        TokenResponse tokenResponse = authService.reissueToken(refreshToken);
                        String newAccessToken = tokenResponse.getAccessToken();
                        
                        // 새 Access Token을 쿠키에 저장 (설정 파일의 만료 시간 사용)
                        Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, jwtUtil.getAccessTokenExpirationInSeconds(), request.isSecure());
                        response.addCookie(accessTokenCookie);
                        
                        log.info("Access Token 자동 재발급 완료");
                        
                        // 새 토큰으로 인증 설정
                        setAuthentication(newAccessToken);
                    } catch (Exception e) {
                        log.warn("자동 재발급 실패: {}", e.getMessage());
                        // 재발급 실패 시 그냥 진행 (인증 실패로 처리됨)
                    }
                } else {
                    log.warn("Refresh Token이 없거나 유효하지 않음");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    // 인증 설정 헬퍼 메서드
    private void setAuthentication(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);
            String name = jwtUtil.getNameFromToken(token);

            // UserPrincipal 객체 생성 (userId, email, name 포함)
            UserPrincipal userPrincipal = new UserPrincipal(userId, email, name);

            // Spring Security 컨텍스트에 인증 정보 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("인증 설정 실패: {}", e.getMessage());
        }
    }

    // 쿠키에서 Refresh Token 추출
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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

    // Authorization 헤더 또는 쿠키에서 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 Bearer 토큰 추출 (기존 방식 지원)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 2. 쿠키에서 Access Token 추출 (새로운 방식)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}

