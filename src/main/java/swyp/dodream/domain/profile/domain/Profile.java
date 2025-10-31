package swyp.dodream.domain.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.profile.enums.ActivityMode;
import swyp.dodream.domain.profile.enums.AgeBand;
import swyp.dodream.domain.profile.enums.Experience;
import swyp.dodream.domain.profile.enums.Gender;
import swyp.dodream.domain.url.domain.ProfileUrl;

import java.util.LinkedHashSet;
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

    @Column(name = "profile_image_code", nullable = false)
    private Integer profileImageCode = 1;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProfileUrl> profileUrls = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Role> roles = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<InterestKeyword> interestKeywords = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<TechSkill> techSkills = new LinkedHashSet<>();

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
                              String introText, Boolean isPublic,Integer profileImageCode) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageBand = ageBand;
        this.experience = experience;
        this.activityMode = activityMode;
        this.introText = introText;
        this.isPublic = isPublic;
        this.profileImageCode = profileImageCode;
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

    public void addProfileUrl(ProfileUrl url) {
        if (url == null) return;
        url.setProfile(this);
        this.profileUrls.add(url);
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

    // 직군/관심/기술 일괄 초기화 편의 메서드
    public void clearRoles() {
        this.roles.clear();
    }

    public void clearInterestKeywords() {
        this.interestKeywords.clear();
    }

    public void clearTechSkills() {
        this.techSkills.clear();
    }

    public void updateProfileImage(Integer code) {
        if (code != null) this.profileImageCode = code;
    }
}
