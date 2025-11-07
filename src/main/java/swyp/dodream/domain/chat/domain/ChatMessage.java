package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.chat.dto.ChatMessageDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // FK: swyp.dodream.domain.user.domain.User
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(name = "deleted_at", nullable = false)
    private Boolean deletedAt;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ReadStatus> readStatuses = new ArrayList<>();

    //  DTO 변환 메서드
    public ChatMessageDto toDto() {
        return new ChatMessageDto(
                this.id,
                this.chatRoom.getId(),
                this.chatRoom.getPostId(),
                this.senderUserId,
                null, // receiverId는 DB에 저장하지 않으므로 DTO 변환 시 null
                this.body,
                this.getCreatedAt(), // BaseEntity의 생성 시간
                ChatMessageDto.MessageType.TALK
        );
    }
}