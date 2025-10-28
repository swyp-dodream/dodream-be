package swyp.dodream.domain;

import swyp.dodream.domain.post.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_triplet", columnNames = {"post_id","leader_user_id","member_user_id"})
        })
public class ChatRoom extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "leader_user_id", nullable = false)
    private Long leaderUserId; // FK to user.id (post.owner_user_id와 동일해야 함)

    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId; // FK to user.id

    @Column(name = "first_message_at")
    private LocalDateTime firstMessageAt;

    /* 생성 규칙: 리더!=멤버, 동일 조합은 재사용(멱등) - 서비스 레벨에서 보장 */
}