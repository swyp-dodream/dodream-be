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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal;
import swyp.dodream.jwt.util.JwtUtil;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
// 토큰 인증을 위한 인터셉터 구현
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final String CHAT_TOPIC_PATTERN = "/topic/chat/post/{postId}/leader/{leaderId}/member/{memberId}";

    // STOMP 메시지가 Channel로 들어오기 직전에 여기서 가로채는 메서드
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 현재 들어온 message의 header를 STOMP 접근자 형태로 캐스팅해서 accessor 변수에 담는다
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP 명령이 CONNECT(처음 websocket 연결 handshake 메시지)일 때만 아래 로직 수행
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("STOMP CONNECT Authorization header: {}", authorizationHeader);
            String token = resolveToken(authorizationHeader);

            if (token != null && jwtUtil.validateToken(token)) {
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String email = jwtUtil.getEmailFromToken(token);
                    String name = jwtUtil.getNameFromToken(token);

                    // [수정] userId가 null인지 반드시 확인
                    if (userId == null) {
                        log.warn("STOMP connection failed: Token is valid but userId is missing from claims.");
                        throw new SecurityException("토큰의 사용자 ID 정보가 유효하지 않습니다.");
                    }
                    // [수정] (선택적) email이나 name도 검사할 수 있습니다.

                    UserPrincipal userPrincipal = new UserPrincipal(userId, email, name);

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("STOMP user authenticated: userId={}, name={}", userId, name);

                } catch (Exception e) {
                    log.warn("STOMP connection failed: Invalid token details", e);
                    // [수정] 예외 메시지를 더 구체적으로 전달
                    throw new SecurityException("Invalid token details: " + e.getMessage());
                }
            } else {
                log.warn("STOMP connection failed: No or Invalid Authorization header");
                throw new SecurityException("Invalid or missing token");
            }
        }
        // SUBSCRIBE 권한 검사 로직
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // CONNECT 시 저장한 인증 정보 가져오기
            Authentication authentication = (Authentication) accessor.getUser();

            // 인증 정보가 없거나, 우리가 저장한 UserPrincipal 타입이 아니면 거부
            if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                log.warn("SUBSCRIBE: 인증되지 않은 사용자 접근");
                throw new SecurityException("인증되지 않은 사용자입니다.");
            }

            // Authentication 에서 현재 WebSocket 연결한 유저 정보를 UserPrincipal로 뽑는다
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // STOMP header에서 destination(topic)을 가져온다
            String destination = accessor.getDestination(); // (e.g., /topic/chat/post/1/leader/10/member/20)

            try {
                if (destination != null) {
                    // 이 userId가 이 destination을 구독해도 되는 유저인지 검사
                    this.validateSubscription(userPrincipal.getUserId(), destination);
                } else {
                    throw new IllegalArgumentException("구독 대상(destination)이 없습니다.");
                }
            } catch (AccessDeniedException e) {
                log.warn("SUBSCRIBE: 권한 없는 토픽 구독 시도. User: {}, Topic: {}", userPrincipal.getUserId(), destination, e);
                // (필수) 예외를 던져 구독을 거부
                throw new SecurityException(e.getMessage());
            } catch (Exception e) {
                log.warn("SUBSCRIBE: 유효하지 않은 구독 시도. User: {}, Topic: {}", userPrincipal.getUserId(), destination, e);
                throw new SecurityException("구독 처리 중 오류 발생: " + e.getMessage());
            }
        }

        // DISCONNECT 처리 시 accessor에 사용자 정보를 보정
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) { // NPE 방지
            // SecurityContextHolder에 들어있는 현재 사용자의 Authentication을 가져온다
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // authentication이 실제로 존재하고
            // STOMP 메시지의 accessor에 붙은 Principal(user) 이 아직 비어있을 때만
            if (authentication != null && accessor.getUser() == null) {
                // STOMP 프레임의 Principal 자리에 Authentication을 그대로 넣어 “누가 DISCONNECT 했는지”를 명확히 남긴다.
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

    /**
     * STOMP 구독(SUBSCRIBE) 요청 시 권한 검사
     *
     * @param userId            인증된 사용자 ID
     * @param topicDestination  구독하려는 토픽 주소
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateSubscription(Long userId, String topicDestination) {

        // 1. 요청된 destination이 우리가 정의한 채팅 토픽 패턴과 일치하는지 확인
        if (!pathMatcher.match(CHAT_TOPIC_PATTERN, topicDestination)) {
            log.warn("유효하지 않은 토픽 구독 시도: {}", topicDestination);
            throw new AccessDeniedException("유효하지 않은 채팅방 토픽입니다.");
        }

        // 2. 토픽 주소에서 leaderId와 memberId 추출
        Map<String, String> uriVariables =
                pathMatcher.extractUriTemplateVariables(CHAT_TOPIC_PATTERN, topicDestination);

        Long leaderId = Long.parseLong(uriVariables.get("leaderId"));
        Long memberId = Long.parseLong(uriVariables.get("memberId"));

        // 3. 현재 사용자가 이 채팅방의 leader 또는 member가 맞는지 확인
        if (!userId.equals(leaderId) && !userId.equals(memberId)) {
            log.warn("채팅방 구독 권한 없음. UserId: {}, Topic: {}", userId, topicDestination);
            throw new AccessDeniedException("이 채팅방을 구독할 권한이 없습니다.");
        }

        // 4. 권한이 있으면 통과
        log.debug("STOMP user subscribed: User: {}, Topic: {}", userId, topicDestination);
    }
}