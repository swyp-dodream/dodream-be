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

    private String profileImageUrl;

    @Column(nullable = false)
    private Boolean status = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public User(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
    
    // Snowflake ID를 사용하는 생성자
    public User(Long id, String name, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
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
