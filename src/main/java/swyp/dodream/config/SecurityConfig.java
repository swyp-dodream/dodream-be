package swyp.dodream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import swyp.dodream.login.service.CustomOAuth2UserService;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    // 인증 없이 접근 가능한 URL 목록
    private static final String[] WHITE_LIST = {
            "/api/login/success",
            "/api/oauth2/authorization/**",
            "/login/oauth2/code/**"
    };

    private static final String[] WHITE_LIST_SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API용)
                .csrf(csrf -> csrf.disable())
                
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
                        .defaultSuccessUrl("/api/login/success", true)
                );

        return http.build();
    }
}

