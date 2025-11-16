package swyp.dodream.domain.application.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "application",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "applicant_id"})
        }
)
public class Application extends BaseEntity {

    @Id
    private Long id; // Snowflake 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 어떤 모집글에 지원했는가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant; // 누가 지원했는가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // 어떤 직군으로 지원했는가

    @Column(name = "application_message", nullable = false, length = 1000)
    private String message; // 지원 메시지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt; // 지원 취소 시각

    // === 생성자 ===
    public Application(Long id, Post post, User applicant, Role role, String message) {
        this.id = id;
        this.post = post;
        this.applicant = applicant;
        this.role = role;
        this.message = message;
        this.status = ApplicationStatus.APPLIED;
    }

    // === 지원 취소 ===
    public void withdraw() {
        if (this.status != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("이미 처리된 지원은 취소할 수 없습니다.");
        }
        this.status = ApplicationStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public void updateReapply(Role role, String message) {
        this.status = ApplicationStatus.APPLIED;
        this.withdrawnAt = null;

        if (role != null) {
            this.role = role;
        }

        if (message != null && !message.isBlank()) {
            this.message = message;
        }
    }
}
