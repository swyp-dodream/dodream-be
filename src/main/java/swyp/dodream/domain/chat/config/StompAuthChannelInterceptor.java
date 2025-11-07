package swyp.dodream.domain.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import swyp.dodream.jwt.dto.UserPrincipal;
import swyp.dodream.jwt.util.JwtUtil;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // CONNECT 요청일 때
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("STOMP CONNECT Authorization header: {}", authorizationHeader);

            String token = resolveToken(authorizationHeader);

            // 토큰이 있고 유효하면 인증 설정
            if (token != null && jwtUtil.validateToken(token)) {
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String email = jwtUtil.getEmailFromToken(token);
                    String name = jwtUtil.getNameFromToken(token);

                    // (주의) swyp.dodream.jwt.dto.UserPrincipal 사용
                    UserPrincipal userPrincipal = new UserPrincipal(userId, email, name);

                    // Spring Security 컨텍스트에 인증 정보 저장
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    // 수정: Authentication 객체를 직접 setUser에 전달
                    // UsernamePasswordAuthenticationToken은 Principal을 구현하고 있음
                    accessor.setUser(authentication);

                    // SecurityContextHolder에도 설정을 해줘야 Service단에서 접근 가능
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("STOMP user authenticated: userId={}, name={}", userId, name);

                } catch (Exception e) {
                    log.warn("STOMP connection failed: Invalid token details", e);
                    throw new SecurityException("Invalid token details");
                }
            } else {
                log.warn("STOMP connection failed: No or Invalid Authorization header");
                // (필수) 인증 실패 시 예외를 던져 연결을 거부
                throw new SecurityException("Invalid or missing token");
            }
        }

        // DISCONNECT 시 SecurityContext 클리어
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && accessor.getUser() == null) {
                accessor.setUser(authentication);
            }
        }

        return message;
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}