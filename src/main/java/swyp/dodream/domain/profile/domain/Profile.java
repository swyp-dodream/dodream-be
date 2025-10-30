package swyp.dodream.domain.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.profile.enums.*;
import swyp.dodream.domain.url.domain.ProfileUrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor
public class Profile extends BaseEntity {

    @Id
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

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfileUrl> profileUrls = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profile_role",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profile_interest_keywords",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_keyword_id")
    )
    private Set<InterestKeyword> interestKeywords = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profile_tech_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "tech_skill_id")
    )
    private Set<TechSkill> techSkills = new HashSet<>();

    public Profile(Long userId, String nickname, Experience experience, ActivityMode activityMode) {
        this.userId = userId;
        this.nickname = nickname;
        this.experience = experience;
        this.activityMode = activityMode;
    }
    
    // Snowflake ID를 사용하는 생성자
    public Profile(Long id, Long userId, String nickname, Experience experience, ActivityMode activityMode) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.experience = experience;
        this.activityMode = activityMode;
    }

    // 전체 프로필 업데이트
    public void updateProfile(String nickname, Gender gender, AgeBand ageBand,
                            Experience experience, ActivityMode activityMode,
                            String introText, Boolean isPublic) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageBand = ageBand;
        this.experience = experience;
        this.activityMode = activityMode;
        this.introText = introText;
        this.isPublic = isPublic;
    }

    // 마이페이지 프로필 정보 업데이트
    public void updateProfileInfo(String nickname, Experience experience, ActivityMode activityMode, String introText) {
        this.nickname = nickname;
        this.experience = experience;
        this.activityMode = activityMode;
        this.introText = introText;
    }

    // 계정 설정 업데이트
    public void updateAccountSettings(Gender gender, AgeBand ageBand, Boolean isPublic) {
        this.gender = gender;
        this.ageBand = ageBand;
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

    // 직군 관리 메서드
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    // 관심 키워드 관리 메서드
    public void addInterestKeyword(InterestKeyword keyword) {
        this.interestKeywords.add(keyword);
    }

    public void removeInterestKeyword(InterestKeyword keyword) {
        this.interestKeywords.remove(keyword);
    }

    // 기술 스킬 관리 메서드
    public void addTechSkill(TechSkill skill) {
        this.techSkills.add(skill);
    }

    public void removeTechSkill(TechSkill skill) {
        this.techSkills.remove(skill);
    }
}
