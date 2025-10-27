package swyp.dodream.domain;

import swyp.dodream.domain.common.ApplicationStatus;
import swyp.dodream.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "application",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_unique", columnNames = {"post_id","applicant_user_id"})
        })
public class Application extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId; // FK to user.id

    @Column(name = "application_message", length = 1000, nullable = false)
    private String applicationMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status; // APPLIED/WITHDRAWN/ACCEPTED/REJECTED

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    /* 도메인 규칙 */
    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
}