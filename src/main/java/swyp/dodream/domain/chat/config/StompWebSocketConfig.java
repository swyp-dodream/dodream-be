package swyp.dodream.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 브로커 기능 활성화
@RequiredArgsConstructor
// STOMP 프로토콜을 기반으로 WebSocket 메시지 브로커를 설정하고 구성할 때
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    // STOMP 엔드포인트 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/connect")
                // 테스트용! 나중에 바꾸기
                .setAllowedOriginPatterns("http://localhost:3000") // 허용할 출처를 설정
                .withSockJS(); // ws://가 아닌 http:// 엔드포인트를 사용할수 있게 해주는 sockJs라이브러리를 통한 요청을 허용하는 설정
    }

    // 메시지 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /publish으로 메시지 발행해야 함을 설정
        // /publish으로 시작하는 url패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic으로 시작하는 형태로 메시지를 수신(subscribe)해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트→서버 inbound STOMP 프레임을 가로채서 JWT 인증/구독 권한 검증 실행
        // interceptor 등록
        registration.interceptors(stompHandler);
    }
}