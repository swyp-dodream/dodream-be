package swyp.dodream.domain.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.domain.profile.enums.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.선택안함;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_band", nullable = false)
    private AgeBand ageBand = AgeBand.선택안함;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Experience experience;

    @Column(name = "role_id")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_mode", nullable = false)
    private ActivityMode activityMode;

    @Column(name = "intro_text", length = 200)
    private String introText;

    @Column(name = "intro_is_ai", nullable = false)
    private Boolean introIsAi = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Profile() {}

    public Profile(Long userId, String nickname, Experience experience, ActivityMode activityMode) {
        this.userId = userId;
        this.nickname = nickname;
        this.experience = experience;
        this.activityMode = activityMode;
    }

    // 전체 프로필 업데이트
    public void updateProfile(String nickname, Gender gender, AgeBand ageBand, 
                            Experience experience, Long roleId, ActivityMode activityMode,
                            String introText, Boolean introIsAi, Boolean isPublic) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageBand = ageBand;
        this.experience = experience;
        this.roleId = roleId;
        this.activityMode = activityMode;
        this.introText = introText;
        this.introIsAi = introIsAi;
        this.isPublic = isPublic;
    }
}
