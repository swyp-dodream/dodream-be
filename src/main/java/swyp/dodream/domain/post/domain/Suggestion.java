package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "to_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suggestion extends BaseEntity {

    @Id
    private Long id;

    @Column(length = 1000, nullable = false)
    private String suggestionMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    public Suggestion(Long id, String suggestionMessage, Post post, User fromUser, User toUser, LocalDateTime createdAt) {
        this.id = id;
        this.suggestionMessage = suggestionMessage;
        this.post = post;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public void withdraw() {
        if (this.withdrawnAt != null) {
            throw new IllegalStateException("이미 취소된 제안입니다.");
        }
        this.withdrawnAt = LocalDateTime.now();
    }
}
