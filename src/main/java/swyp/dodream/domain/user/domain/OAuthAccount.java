package swyp.dodream.domain.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.login.domain.AuthProvider;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String email;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    protected OAuthAccount() {}

    public OAuthAccount(Long userId, AuthProvider provider, String email) {
        this.userId = userId;
        this.provider = provider;
        this.email = email;
        this.lastLoginAt = LocalDateTime.now();
    }

    // 최근 로그인 시간 업데이트
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
