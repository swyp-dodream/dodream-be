package swyp.dodream.login.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;

/**
 * OAuth2 로그인 시작 시 프론트엔드 URL을 자동으로 감지하여 쿠키에 저장하는 필터
 * 
 * 역할:
 * - OAuth2 로그인 시작 요청(/oauth2/authorization/**) 또는 콜백 요청(/login/oauth2/code/**)에서
 * - Referer 또는 Origin 헤더에서 프론트엔드 URL을 추출하여 쿠키에 저장
 * - OAuth2SuccessHandler에서 이 쿠키 값을 사용하여 적절한 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
public class OAuth2FrontendUrlFilter extends OncePerRequestFilter {

    private static final String FRONTEND_URL_COOKIE = "OAUTH2_FRONTEND_URL";
    private static final int COOKIE_MAX_AGE = 300; // 5분

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // OAuth2 로그인 시작 요청 또는 콜백 요청인지 확인
        boolean isOAuth2Request = (requestURI != null && 
                (requestURI.startsWith("/oauth2/authorization/") || 
                 requestURI.startsWith("/login/oauth2/code/")));
        
        if (isOAuth2Request) {
            // 쿠키에 이미 저장된 값이 있으면 스킵
            String existingCookie = getFrontendUrlFromCookie(request);
            if (existingCookie != null && !existingCookie.isEmpty()) {
                log.debug("이미 쿠키에 프론트엔드 URL이 저장되어 있음: {}", existingCookie);
                filterChain.doFilter(request, response);
                return;
            }

            // 프론트엔드 URL 추출 우선순위: Origin > Referer > Host 헤더 기반 추론
            String frontendUrl = extractFrontendUrlFromOrigin(request);
            
            // Origin이 없으면 Referer에서 추출 시도
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = extractFrontendUrlFromReferer(request);
            }
            
            // Origin과 Referer가 모두 없으면 Host 헤더 기반으로 추론
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = inferFrontendUrlFromHost(request);
            }

            // 프론트엔드 URL을 찾았으면 쿠키에 저장
            if (frontendUrl != null && !frontendUrl.trim().isEmpty()) {
                try {
                    Cookie cookie = new Cookie(FRONTEND_URL_COOKIE, frontendUrl);
                    cookie.setPath("/");
                    cookie.setMaxAge(COOKIE_MAX_AGE);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(request.isSecure()); // HTTPS인 경우에만 secure
                    response.addCookie(cookie);

                    log.info("OAuth2 프론트엔드 URL 자동 감지 및 쿠키 저장: {} (Origin/Referer/Host에서 추출)", frontendUrl);
                } catch (Exception e) {
                    log.warn("프론트엔드 URL 쿠키 저장 실패: {}", e.getMessage());
                }
            } else {
                log.debug("프론트엔드 URL을 자동으로 감지할 수 없음 (Origin/Referer/Host 헤더 없음)");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Origin 헤더에서 프론트엔드 URL 추출
     */
    private String extractFrontendUrlFromOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.trim().isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(origin);
            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();

            // 포트가 기본 포트가 아니면 포함
            if (port != -1 && !((protocol.equals("http") && port == 80) || (protocol.equals("https") && port == 443))) {
                return String.format("%s://%s:%d", protocol, host, port);
            }

            return String.format("%s://%s", protocol, host);
        } catch (Exception e) {
            log.debug("Origin 헤더에서 프론트엔드 URL 추출 실패: {}", origin, e);
            return null;
        }
    }

    /**
     * Referer 헤더에서 프론트엔드 URL 추출
     */
    private String extractFrontendUrlFromReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.trim().isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(referer);
            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();

            // 포트가 기본 포트가 아니면 포함
            if (port != -1 && !((protocol.equals("http") && port == 80) || (protocol.equals("https") && port == 443))) {
                return String.format("%s://%s:%d", protocol, host, port);
            }

            return String.format("%s://%s", protocol, host);
        } catch (Exception e) {
            log.debug("Referer 헤더에서 프론트엔드 URL 추출 실패: {}", referer, e);
            return null;
        }
    }

    /**
     * Host 헤더 기반으로 프론트엔드 URL 추론
     * api.dodream.store -> dodream.store
     * api.dev.dodream.store -> dev.dodream.store (만약 존재한다면)
     */
    private String inferFrontendUrlFromHost(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null || host.trim().isEmpty()) {
            host = request.getServerName();
        }
        
        if (host == null || host.trim().isEmpty()) {
            return null;
        }
        
        try {
            // api.dodream.store -> dodream.store
            // api.dev.dodream.store -> dev.dodream.store
            String frontendHost = host;
            if (host.startsWith("api.")) {
                frontendHost = host.substring(4); // "api." 제거
            } else if (host.startsWith("api-")) {
                // api-dev.dodream.store 같은 경우는 그대로 유지
                frontendHost = host;
            }
            
            // 프로토콜 결정 (요청이 HTTPS인지 확인)
            String protocol = request.isSecure() || 
                             request.getHeader("X-Forwarded-Proto") != null && 
                             request.getHeader("X-Forwarded-Proto").equals("https") 
                             ? "https" : "http";
            
            // 포트 확인
            int port = request.getServerPort();
            if (port != -1 && port != 80 && port != 443) {
                return String.format("%s://%s:%d", protocol, frontendHost, port);
            }
            
            return String.format("%s://%s", protocol, frontendHost);
        } catch (Exception e) {
            log.debug("Host 헤더에서 프론트엔드 URL 추론 실패: {}", host, e);
            return null;
        }
    }
    
    /**
     * 쿠키에서 프론트엔드 URL 가져오기
     */
    private String getFrontendUrlFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (FRONTEND_URL_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}

