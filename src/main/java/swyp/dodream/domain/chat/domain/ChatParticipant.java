package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_participant")
@IdClass(ChatParticipantId.class)
public class ChatParticipant {

    @Id
    @Column(name = "room_id", nullable = false)
    private String chatRoomId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    // 생성자
    public ChatParticipant(ChatRoom chatRoom, Long userId) {
        this.chatRoom = chatRoom;
        this.chatRoomId = chatRoom.getId();  // String
        this.userId = userId;
        this.joinedAt = LocalDateTime.now();
    }

    // 나가기
    public void leave() {
        this.leftAt = LocalDateTime.now();
    }
}