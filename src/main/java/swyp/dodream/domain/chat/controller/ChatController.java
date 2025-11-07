package swyp.dodream.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @AuthenticationPrincipal UserPrincipal userPrincipal // (Spring Security에서 인증된 유저 정보)
    ) {
        if (userPrincipal == null) {
            // (혹은 SecurityConfig에서 처리)
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        // userPrincipal.getUserId() 사용
        Long memberId = userPrincipal.getUserId();
        ChatInitiateResponse response = chatService.initiateChat(request.getPostId(), memberId);
        return ResponseEntity.ok(response);
    }

    // 채팅방 나가기
    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
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

    // getMyChatRooms: @AuthenticationPrincipal 추가
    @GetMapping("/my/rooms")
    public ResponseEntity<List<MyChatListResponse>> getMyChatRooms( // 반환 타입 명시
                                                                    @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        List<MyChatListResponse> myChatListResDtos = chatService.getMyChatRooms(userPrincipal.getUserId());
        return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
    }

    // messageRead: 메시지 읽음 처리 API (POST 또는 PUT 사용)
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> readMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        chatService.messageRead(roomId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    // getChatHistory: 채팅 내역 조회 API
    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }

        List<ChatMessageDto> history = chatService.getChatHistory(roomId, userPrincipal.getUserId());
        return ResponseEntity.ok(history);
    }
}