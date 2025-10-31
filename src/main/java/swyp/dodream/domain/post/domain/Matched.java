package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.CancelBy;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "matched",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matched {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt = LocalDateTime.now();

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "canceled_by")
    private CancelBy canceledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason_code")
    private CancelReasonCode cancelReasonCode;

    @Column(name = "canceled")
    private boolean canceled = false;

    @Builder
    public Matched(Long id, Post post, User user, Application application,
                   LocalDateTime matchedAt, boolean canceled) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.application = application;  // NULL 가능!
        this.matchedAt = matchedAt != null ? matchedAt : LocalDateTime.now();
        this.canceled = canceled;
    }

    /**
     * 작성자인지 확인 (application_id가 NULL이면 작성자)
     */
    public boolean isOwner() {
        return this.application == null;
    }
}