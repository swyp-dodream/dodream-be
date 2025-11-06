package swyp.dodream.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // /topic으로 시작하는 대상(destination)을 구독하는 클라이언트에게 메시지를 브로드캐스트
        registry.enableSimpleBroker("/topic");
        // /pub으로 시작하는 메시지는 @MessageMapping이 붙은 메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 엔드포인트 설정
        // 클라이언트가 WebSocket에 연결할 엔드포인트
        registry.addEndpoint("/ws-stomp") // (예: /api/ws-stomp)
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS(); // SockJS 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트 인바운드 채널에 인터셉터 등록
        // WebSocket 연결 시 JWT 인증을 처리하기 위함
        registration.interceptors(stompAuthChannelInterceptor);
    }
}