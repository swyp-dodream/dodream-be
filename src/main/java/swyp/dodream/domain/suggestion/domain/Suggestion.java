package swyp.dodream.domain.suggestion.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.master.domain.SuggestionStatus;
import swyp.dodream.domain.post.domain.Post;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status = SuggestionStatus.SENT;

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
        this.status = SuggestionStatus.CANCELED;
        this.withdrawnAt = LocalDateTime.now();
    }

    public void markAsAccepted() {
        this.status = SuggestionStatus.ACCEPTED;
    }

    public void markAsRejected() {
        this.status = SuggestionStatus.REJECTED;
    }

    public void resend(String suggestionMessage) {
        // 다시 보낼 수 있는 상태만 허용(CANCELED, REJECTED)
        if (this.status != SuggestionStatus.CANCELED && this.status != SuggestionStatus.REJECTED) {
            throw new IllegalStateException("현재 상태에서는 제안을 다시 보낼 수 없습니다.");
        }

        this.status = SuggestionStatus.SENT;
        this.withdrawnAt = null;

        if (suggestionMessage != null && !suggestionMessage.isBlank()) {
            this.suggestionMessage = suggestionMessage;
        }
    }
}
