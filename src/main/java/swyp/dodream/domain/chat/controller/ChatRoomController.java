package swyp.dodream.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.chat.dto.ChatInitiateRequest;
import swyp.dodream.domain.chat.dto.ChatInitiateResponse;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal; // (주의) 실제 UserPrincipal 임포트

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat") // (API 엔드포인트는 프로젝트에 맞게 수정)
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * CHAT-01, BR-02-02: '채팅하기' 버튼 클릭
     * 채팅방을 초기화(조회 또는 생성 준비)하고,
     * WebSocket 구독 정보 및 기존 대화 내역을 반환합니다.
     */
    @PostMapping("/initiate")
    public ResponseEntity<ChatInitiateResponse> initiateChat(
            @RequestBody ChatInitiateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal // (Spring Security에서 인증된 유저 정보)
    ) {
        if (userPrincipal == null) {
            // (혹은 SecurityConfig에서 처리)
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        // (주의) userPrincipal.getUserId() 사용
        Long memberId = userPrincipal.getUserId();
        ChatInitiateResponse response = chatService.initiateChat(request.getPostId(), memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * CHAT-02: 채팅방 나가기
     */
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChat(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        Long userId = userPrincipal.getUserId();
        chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    // (기타 엔드포인트: 채팅방 목록 조회 등)
}