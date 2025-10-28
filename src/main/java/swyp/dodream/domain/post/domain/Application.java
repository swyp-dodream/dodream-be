package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "application")
public class Application {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 어떤 모집글에 지원했는가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant; // 누가 지원했는가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // 어떤 직군으로 지원했는가

    @Column(nullable = false, length = 500)
    private String message; // 지원 메시지

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Application(Post post, User applicant, Role role, String message) {
        this.id = null; // Snowflake로 설정
        this.post = post;
        this.applicant = applicant;
        this.role = role;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
