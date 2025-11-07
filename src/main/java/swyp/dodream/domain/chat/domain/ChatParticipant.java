package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_participant")
@IdClass(ChatParticipantId.class)
public class ChatParticipant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public ChatParticipant(ChatRoom chatRoom, Long userId) {
        this.chatRoom = chatRoom;
        this.userId = userId;
        this.leftAt = null; // 생성 시점에는 나가지 않은 상태
    }
}