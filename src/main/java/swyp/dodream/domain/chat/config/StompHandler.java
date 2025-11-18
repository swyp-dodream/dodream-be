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
import org.springframework.stereotype.Component;
import swyp.dodream.jwt.dto.UserPrincipal;
import swyp.dodream.jwt.util.JwtUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.warn("StompHeaderAccessor is null!");
            return message;
        }

        StompCommand command = accessor.getCommand();
        log.debug("STOMP Command: {}", command);

        // CONNECT와 SEND 모두 처리
        if (StompCommand.CONNECT.equals(command) || StompCommand.SEND.equals(command)) {

            log.info("=== STOMP {} 처리 시작 ===", command);

            String authToken = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization 헤더: {}", authToken != null ? "존재함" : "없음");

            if (authToken == null || !authToken.startsWith("Bearer ")) {
                log.error("{}: Authorization 헤더 누락 또는 형식 오류", command);
                return message; // SEND는 예외를 던지지 않고 그냥 통과시킴
            }

            String token = authToken.substring(7);
            log.debug("토큰 길이: {}, 앞 20자: {}...", token.length(), token.substring(0, Math.min(20, token.length())));

            // JWT 유효성 검증
            boolean isValid = jwtUtil.validateToken(token);
            log.info("JWT 유효성 검증 결과: {}", isValid);

            if (!isValid) {
                log.error("{}:유효하지 않은 JWT 토큰!", command);
                return message; // SEND는 예외를 던지지 않고 그냥 통과시킴
            }

            try {
                log.debug("JWT에서 사용자 정보 추출 시작...");

                Long userId = jwtUtil.getUserIdFromToken(token);
                log.debug("추출된 userId: {}", userId);

                String email = jwtUtil.getEmailFromToken(token);
                log.debug("추출된 email: {}", email);

                String name = jwtUtil.getNameFromToken(token);
                log.debug("추출된 name: {}", name);

                UserPrincipal userPrincipal = new UserPrincipal(userId, email, name);
                log.info("UserPrincipal 생성 완료: {}", userPrincipal);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        null
                );

                accessor.setUser(authentication);
                log.info("STOMP {} 인증 완료 - UserId: {}", command, userId);

            } catch (Exception e) {
                log.error("{}: JWT 토큰 파싱 실패 - {}: {}", command, e.getClass().getSimpleName(), e.getMessage(), e);
                return message; // 예외 발생 시에도 그냥 통과
            }
        }

        return message;
    }
}