package swyp.dodream.domain.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_room", uniqueConstraints = {
        // 한 게시글에 대해 리더-멤버 간 채팅방은 유일해야 함
        @UniqueConstraint(
                name = "UK_post_leader_member",
                columnNames = {"post_id", "leader_user_id", "member_user_id"}
        )
})
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: swyp.dodream.domain.post.domain.Post
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // FK: swyp.dodream.domain.user.domain.User (게시글 작성자)
    @Column(name = "leader_user_id", nullable = false)
    private Long leaderUserId;

    // FK: swyp.dodream.domain.user.domain.User (채팅 상대)
    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "first_message_at")
    private LocalDateTime firstMessageAt;
}