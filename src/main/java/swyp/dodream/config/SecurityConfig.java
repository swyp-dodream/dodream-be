package swyp.dodream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import swyp.dodream.login.handler.OAuth2SuccessHandler;
import swyp.dodream.login.service.CustomOAuth2UserService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;

    // 인증 없이 접근 가능한 URL 목록
    private static final String[] WHITE_LIST = {
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/api/auth/reissue",
            "/api/profiles/intro/ai-draft"
    };

    private static final String[] WHITE_LIST_SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                         OAuth2SuccessHandler oAuth2SuccessHandler,
                         JwtUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 비활성화 (REST API용)
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 사용하지 않음 (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                
                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 모든 OPTIONS 메서드 요청은 인증 없이 허용합니다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // WHITE_LIST에 있는 URL은 인증 없이 접근 가능
                        .requestMatchers(
                                Stream.of(WHITE_LIST, WHITE_LIST_SWAGGER)
                                        .flatMap(Arrays::stream)
                                        .toArray(String[]::new)
                        ).permitAll()
                        // WebSocket(SockJS) 핸드셰이크를 위한 경로 허용
                        .requestMatchers("/connect/**").permitAll()
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

    // CORS 설정을 위한 Bean 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // (중요) Vue 서버 주소인 http://localhost:3000 허용
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드 (GET, POST 등)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));

        // (중요) 인증 정보(쿠키, Authorization 헤더 등) 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // "/api/**" 경로에 대해 위 설정 적용
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}


