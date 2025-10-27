package swyp.dodream.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import swyp.dodream.domain.common.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post",
        indexes = {
                @Index(name = "idx_post_status_deadline", columnList = "status, deadlineAt"),
                @Index(name = "idx_post_owner", columnList = "ownerUserId")
        })
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId; // FK to user.id

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    private ProjectType projectType; // PROJECT/STUDY

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_mode", nullable = false)
    private ActivityMode activityMode; // ONLINE/OFFLINE/HYBRID

    @Column(name = "duration_text", length = 100)
    private String durationText;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status; // RECRUITING/COMPLETED

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "deleted_at")
    private Boolean deletedAt; // 스키마 명칭을 그대로 매핑 (실제 의미: 삭제 여부)

    /* 편의 메서드 */
    public boolean isClosedByDeadline(LocalDateTime now) {
        return deadlineAt != null && deadlineAt.isBefore(now);
    }

    public void close() { this.status = PostStatus.COMPLETED; }
}
