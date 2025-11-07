package swyp.dodream.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.dto.request.ChatInitiateRequest;
import swyp.dodream.domain.chat.dto.response.ChatInitiateResponse;
import swyp.dodream.domain.chat.dto.response.MyChatListResponse;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 개설 또는 기존 roomId return
    @PostMapping("/room/create")
    public ResponseEntity<ChatInitiateResponse> initiateChat(
            @RequestBody ChatInitiateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long memberId = userPrincipal.getUserId();

        ChatInitiateResponse response = chatService.initiateChat(request.getPostId(), memberId);
        return ResponseEntity.ok(response);
    }

    // 채팅방 나가기
    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable String roomId,  // String
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    // 내 채팅방 목록
    @GetMapping("/my/rooms")
    public ResponseEntity<List<MyChatListResponse>> getMyChatRooms(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        List<MyChatListResponse> response = chatService.getMyChatRooms(userId);
        return ResponseEntity.ok(response);
    }

    // 메시지 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> readMessages(
            @PathVariable String roomId,  // String
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        chatService.messageRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    // 채팅 내역 조회
    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable String roomId,  // String
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        List<ChatMessageDto> history = chatService.getChatHistory(roomId, userId);
        return ResponseEntity.ok(history);
    }

    // --- 인증 검증 헬퍼 ---
    private void validateAuthentication(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }
    }
}