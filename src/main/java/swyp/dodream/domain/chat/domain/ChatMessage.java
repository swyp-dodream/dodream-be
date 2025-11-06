package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import swyp.dodream.domain.chat.dto.ChatMessageDto;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_message", indexes = {
        // 메시지 조회 성능을 위한 인덱스
        @Index(name = "idx_room_created_at", columnList = "room_id, created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: ChatRoom
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    // FK: swyp.dodream.domain.user.domain.User
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Lob // TEXT 타입
    @Column(name = "body", nullable = false)
    private String body;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ERD 명세에 따른 boolean (soft delete)
    @Column(name = "deleted_at", nullable = false)
    private boolean deletedAt = false;

    /**
     * Entity를 DTO로 변환합니다.
     */
    public ChatMessageDto toDto() {
        return new ChatMessageDto(
                this.id,
                this.roomId,
                null, // postId는 메시지 전송 시 DTO에 필요 없음
                this.senderUserId,
                null, // receiverId는 DTO에만 존재
                this.body,
                this.createdAt,
                ChatMessageDto.MessageType.TALK
        );
    }
}