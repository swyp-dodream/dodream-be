package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.common.snowflake.SnowflakeIdService;
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
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(name = "deleted_at", nullable = false)
    private Boolean deletedAt = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TALK;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ReadStatus> readStatuses = new ArrayList<>();

    public enum MessageType {
        TALK,
        LEAVE
    }

    public ChatMessageDto toDto() {
        // receiverId 계산: sender가 아닌 다른 참여자
        Long receiverId = this.senderUserId.equals(this.chatRoom.getLeaderUserId())
                ? this.chatRoom.getMemberUserId()
                : this.chatRoom.getLeaderUserId();

        return new ChatMessageDto(
                this.id,
                this.chatRoom.getId(),
                String.valueOf(this.chatRoom.getPostId()),
                String.valueOf(this.senderUserId),
                String.valueOf(receiverId),
                null,  // senderNickname - Service에서 채워줌
                this.body,
                this.getCreatedAt(),
                ChatMessageDto.MessageType.valueOf(this.messageType.name())
        );
    }
}