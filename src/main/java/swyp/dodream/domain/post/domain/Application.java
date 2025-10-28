package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import swyp.dodream.domain.post.common.ApplicationStatus;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "application",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "applicant_user_id"}))
public class Application {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private User applicant;

    @Column(length = 1000, nullable = false)
    private String applicationMessage;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime withdrawnAt;

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
}
