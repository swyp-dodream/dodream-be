package swyp.dodream.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;

    // --- 방 식별자 (둘 중 하나만 필요) ---
    private String roomId;           // 기존 방의 ID (첫 메시지 이후)
    private String postId;           // 첫 메시지를 보낼 때 사용 (roomId가 null일 경우)

    // --- 참여자 식별자 ---
    private String senderId;         // 보낸 사람
    private String receiverId;       // 받는 사람

    // --- 메시지 내용 ---
    private String body;
    private LocalDateTime createdAt;
    private MessageType messageType;

    public enum MessageType {
        TALK, LEAVE
    }
}