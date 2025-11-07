package swyp.dodream.domain.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.common.snowflake.SnowflakeIdService;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private swyp.dodream.login.domain.AuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private Boolean status = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public User(String name, String email, String profileImageUrl, swyp.dodream.login.domain.AuthProvider provider, String providerId) {
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }
    
    // Snowflake ID를 사용하는 생성자
    public User(Long id, String name, String email, String profileImageUrl, swyp.dodream.login.domain.AuthProvider provider, String providerId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }

    // Update methods
    public void updateProfile(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    // 회원탈퇴 (소프트 딜리션)
    public void withdraw() {
        this.status = false;
    }
}
