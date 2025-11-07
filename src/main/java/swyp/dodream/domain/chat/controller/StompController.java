package swyp.dodream.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(
            ChatMessageDto messageDto,
            Principal principal  // ⭐ @AuthenticationPrincipal 대신 Principal 사용
    ) {
        log.info("=== 메시지 핸들러 진입 ===");
        log.info("MessageDto: {}", messageDto);
        log.info("Principal 타입: {}", principal != null ? principal.getClass().getName() : "null");
        log.info("Principal: {}", principal);

        // Principal에서 UserPrincipal 추출
        UserPrincipal userPrincipal = extractUserPrincipal(principal);

        if (userPrincipal == null) {
            log.error("❌ UserPrincipal 추출 실패!");
            throw new SecurityException("인증 정보가 없습니다.");
        }

        log.info("✅ UserPrincipal 추출 성공: {}", userPrincipal);

        Long senderId = userPrincipal.getUserId();
        log.info("추출된 SenderId: {}", senderId);

        if (senderId == null) {
            log.error("❌ userId가 null입니다!");
            throw new IllegalStateException("유효하지 않은 사용자 ID입니다.");
        }

        try {
            log.info("✅ 채팅 메시지 처리 시작 - SenderId: {}, Message: {}", senderId, messageDto.getBody());
            chatService.processMessage(messageDto, senderId);
            log.info("✅ 채팅 메시지 처리 완료 - SenderId: {}", senderId);
        } catch (Exception e) {
            log.error("❌ 채팅 메시지 처리 중 오류 발생 - SenderId: {}, Error: {}", senderId, e.getMessage(), e);
            throw new RuntimeException("메시지 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * Principal에서 UserPrincipal 추출
     */
    private UserPrincipal extractUserPrincipal(Principal principal) {
        if (principal == null) {
            log.error("Principal이 null입니다!");
            return null;
        }

        log.debug("Principal 타입 확인: {}", principal.getClass().getName());

        // UsernamePasswordAuthenticationToken인 경우
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            Object principalObj = auth.getPrincipal();

            log.debug("Principal 객체 타입: {}", principalObj != null ? principalObj.getClass().getName() : "null");

            if (principalObj instanceof UserPrincipal) {
                return (UserPrincipal) principalObj;
            } else {
                log.error("Principal 객체가 UserPrincipal이 아닙니다: {}", principalObj);
            }
        } else {
            log.error("Principal이 UsernamePasswordAuthenticationToken이 아닙니다: {}", principal.getClass().getName());
        }

        return null;
    }
}