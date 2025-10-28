package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import swyp.dodream.domain.post.common.CancelBy;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "matched",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
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
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    private LocalDateTime matchedAt = LocalDateTime.now();
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    private CancelBy canceledBy;

    @Enumerated(EnumType.STRING)
    private CancelReasonCode cancelReasonCode;

    private boolean canceled = false;
}