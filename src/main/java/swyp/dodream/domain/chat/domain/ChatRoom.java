package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import swyp.dodream.common.snowflake.SnowflakeIdService;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "leader_user_id", "member_user_id"})
})
public class ChatRoom {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "leader_user_id", nullable = false)
    private Long leaderUserId;

    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId;

    @Column(name = "first_message_at")
    private LocalDateTime firstMessageAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ChatRoom(Long postId, Long leaderUserId, Long memberUserId, SnowflakeIdService snowflakeIdService) {
        this.id = snowflakeIdService.nextStringId();  // String ID
        this.postId = postId;
        this.leaderUserId = leaderUserId;
        this.memberUserId = memberUserId;
        this.createdAt = LocalDateTime.now();
    }

    public void setFirstMessageAt(LocalDateTime firstMessageAt) {
        this.firstMessageAt = firstMessageAt;
    }
}