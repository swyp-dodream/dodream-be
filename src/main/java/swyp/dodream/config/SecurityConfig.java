package swyp.dodream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import swyp.dodream.jwt.filter.JwtAuthenticationFilter;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.login.filter.OAuth2FrontendUrlFilter;
import swyp.dodream.login.handler.OAuth2SuccessHandler;
import swyp.dodream.login.service.CustomOAuth2UserService;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;
    private final OAuth2FrontendUrlFilter oAuth2FrontendUrlFilter;

    // 인증 없이 접근 가능한 URL 목록
    private static final String[] WHITE_LIST = {
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/api/auth/reissue",
            "/api/auth/oauth2/authorize/**",  // OAuth2 로그인 시작 (쿠키 설정용)
            "/api/profiles/intro/ai-draft",
            "/api/dev/**"  // 테스트용: 개발 시드 데이터 생성 API
    };

    private static final String[] WHITE_LIST_SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                         OAuth2SuccessHandler oAuth2SuccessHandler,
                         JwtUtil jwtUtil,
                         OAuth2FrontendUrlFilter oAuth2FrontendUrlFilter) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtUtil = jwtUtil;
        this.oAuth2FrontendUrlFilter = oAuth2FrontendUrlFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // JWT 인증 필터 생성
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);
        
        http
                // CSRF 비활성화 (REST API용)
                .csrf(csrf -> csrf.disable())
                
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 세션 사용하지 않음 (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // JWT 인증 필터 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                
                // OAuth2 프론트엔드 URL 자동 감지 필터 추가 (JWT 필터보다 먼저 실행)
                .addFilterBefore(oAuth2FrontendUrlFilter, jwtFilter.getClass())
                
                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // WHITE_LIST에 있는 URL은 인증 없이 접근 가능
                        .requestMatchers(
                                Stream.of(WHITE_LIST, WHITE_LIST_SWAGGER)
                                        .flatMap(Arrays::stream)
                                        .toArray(String[]::new)
                        ).permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    /**
     * CORS 설정
     * 프론트엔드 도메인에서의 요청을 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin (프론트엔드 도메인)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://dodream.store",
                "https://www.dodream.store",
                "https://dev.dodream.store"
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // 인증 정보(쿠키, Authorization 헤더 등) 허용
        configuration.setAllowCredentials(true);
        
        // Preflight 요청의 결과를 캐시할 시간(초)
        configuration.setMaxAge(3600L);
        
        // 응답 헤더에 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}


