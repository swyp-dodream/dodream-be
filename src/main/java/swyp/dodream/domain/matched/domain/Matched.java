package swyp.dodream.domain.matched.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.post.common.CancelBy;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.post.domain.Post;
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

    @Column(name = "is_canceled")
    private boolean isCanceled = false;

    @Builder
    public Matched(Long id, Post post, User user, Application application,
                   LocalDateTime matchedAt, boolean isCanceled) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.application = application;  // NULL 가능!
        this.matchedAt = matchedAt != null ? matchedAt : LocalDateTime.now();
        this.isCanceled = isCanceled;
    }

    /**
     * 작성자인지 확인 (application_id가 NULL이면 작성자)
     */
    public boolean isOwner() {
        return this.application == null;
    }

    // 매칭 취소
    public void cancel(CancelBy by, CancelReasonCode reason) {
        if (Boolean.TRUE.equals(this.isCanceled)) {
            throw new IllegalStateException("이미 취소된 매칭입니다.");
        }
        this.isCanceled = true;
        this.canceledBy = by;
        this.cancelReasonCode = reason;
        this.canceledAt = LocalDateTime.now();
    }

    // 매칭된 지 24시간 이내인지 확인
    public boolean isWithin24h() {
        return matchedAt != null && LocalDateTime.now().isBefore(matchedAt.plusHours(24));
    }
}