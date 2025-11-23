package swyp.dodream.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.chat.domain.ChatFilterType;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.dto.request.ChatInitiateRequest;
import swyp.dodream.domain.chat.dto.response.ChatInitiateResponse;
import swyp.dodream.domain.chat.dto.response.MessageReadResponse;
import swyp.dodream.domain.chat.dto.response.MyChatListResponse;
import swyp.dodream.domain.chat.service.ChatService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "1:1 채팅 API")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 개설 또는 기존 roomId return
    @Operation(
            summary = "채팅방 개설 또는 기존 roomId 조회",
            description = "지원 유저가 Post 상세에서 채팅하기 버튼 눌렀을 때 호출. 이미 리더와 1:1 채팅방이 있으면 같은 roomId를 반환한다"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음")
    })
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
    @Operation(
            summary = "채팅방 나가기",
            description = "해당 roomId에서 나간다."
    )
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
    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "로그인한 사용자가 포함된 모든 채팅방 목록을 최신순으로 조회. filter 파라미터로 전체(ALL)/읽지않음(UNREAD) 필터링 가능"
    )
    @GetMapping("/my/rooms")
    public ResponseEntity<List<MyChatListResponse>> getMyChatRooms(
            @RequestParam(defaultValue = "ALL") ChatFilterType filter,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        List<MyChatListResponse> response = chatService.getMyChatRooms(userId, filter);
        return ResponseEntity.ok(response);
    }

    // 메시지 읽음 처리
    @Operation(
            summary = "메시지 읽음 처리",
            description = "특정 채팅방 내 아직 unread 상태인 메시지를 읽음 처리"
    )
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<MessageReadResponse> readMessages(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        int readCount = chatService.messageRead(roomId, userId);
        return ResponseEntity.ok(new MessageReadResponse(readCount));
    }

    // 채팅 내역 조회
    @Operation(
            summary = "채팅 메시지 조회 (페이지네이션)",
            description = "채팅방의 메시지를 페이지네이션으로 조회. lastMessageId 파라미터 없으면 최신 메시지, 있으면 해당 ID 이전 메시지 조회"
    )
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable String roomId,
            @RequestParam(required = false) String lastMessageId,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        validateAuthentication(userPrincipal);
        Long userId = userPrincipal.getUserId();

        List<ChatMessageDto> messages;
        if (lastMessageId == null) {
            // 최신 메시지
            messages = chatService.getRecentMessages(roomId, userId, size);
        } else {
            // 특정 ID 이전 메시지
            messages = chatService.getMessagesBeforeId(roomId, userId, lastMessageId, size);  // 🔥 변경
        }

        return ResponseEntity.ok(messages);
    }

    // --- 인증 검증 헬퍼 ---
    private void validateAuthentication(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw ExceptionType.UNAUTHORIZED_NO_AUTHENTICATION.of();
        }
    }
}