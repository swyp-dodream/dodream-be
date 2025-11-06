package swyp.dodream.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal; // (주의) 실제 UserPrincipal 임포트

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * WebSocket 메시지 수신
     * 클라이언트가 /pub/chat/message (WebSocketConfig 참조)로 메시지를 보내면
     * 이 메서드가 호출되어 메시지를 처리합니다.
     */
    @MessageMapping("/chat/message")
    public void message(
            ChatMessageDto messageDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal // (StompAuthChannelInterceptor에서 설정한 인증 정보)
    ) {
        if (userPrincipal == null) {
            log.warn("인증되지 않은 사용자의 메시지 수신 시도. (STOMP)");
            // 인터셉터에서 차단되지만, 방어 코드
            return;
        }

        // (주의) userPrincipal.getUserId() 사용
        Long senderId = userPrincipal.getUserId();
        log.debug("메시지 수신: SenderId: {}, DTO: {}", senderId, messageDto);

        // ChatService로 메시지 처리 위임 (DB 저장 및 브로드캐스트)
        chatService.processMessage(messageDto, senderId);
    }
}