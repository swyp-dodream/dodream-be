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
@IdClass(ChatParticipantId.class) // 복합키 클래스 지정
public class ChatParticipant {

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public ChatParticipant(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
        this.leftAt = null; // 생성 시점에는 나가지 않은 상태
    }
}