package swyp.dodream.domain.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.domain.profile.enums.*;
import swyp.dodream.domain.url.domain.ProfileUrl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_mode", nullable = false)
    private ActivityMode activityMode;

    @Column(name = "intro_text", length = 200)
    private String introText;

    @Column(name = "intro_is_ai", nullable = false)
    private Boolean introIsAi = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfileUrl> profileUrls = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Profile(Long userId, String nickname, Experience experience, ActivityMode activityMode) {
        this.userId = userId;
        this.nickname = nickname;
        this.experience = experience;
        this.activityMode = activityMode;
    }

    // 전체 프로필 업데이트
    public void updateProfile(String nickname, Gender gender, AgeBand ageBand, 
                            Experience experience, ActivityMode activityMode,
                            String introText, Boolean introIsAi, Boolean isPublic) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageBand = ageBand;
        this.experience = experience;
        this.activityMode = activityMode;
        this.introText = introText;
        this.introIsAi = introIsAi;
        this.isPublic = isPublic;
    }

    // ProfileUrl 관리 메서드들
    public void addProfileUrl(ProfileUrl profileUrl) {
        this.profileUrls.add(profileUrl);
        profileUrl.setProfile(this);
    }

    public void removeProfileUrl(ProfileUrl profileUrl) {
        this.profileUrls.remove(profileUrl);
        profileUrl.setProfile(null);
    }
}
