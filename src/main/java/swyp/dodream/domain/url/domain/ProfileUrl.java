package swyp.dodream.domain.url.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.url.enums.UrlLabel;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_urls")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileUrl {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrlLabel label;

    @Column(nullable = false, length = 500)
    private String url;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Set 중복 방지를 위한 고유 식별 키 정의
    @EqualsAndHashCode.Include
    private String uniqueKey() {
        return (profile != null ? profile.getId() : 0L) + "|" + label.name() + "|" + url;
    }

    public ProfileUrl(Profile profile, UrlLabel label, String url) {
        this.profile = profile;
        this.label = label;
        this.url = url;
    }

    // Snowflake ID를 사용하는 생성자
    public ProfileUrl(Long id, Profile profile, UrlLabel label, String url) {
        this.id = id;
        this.profile = profile;
        this.label = label;
        this.url = url;
    }

    public void updateUrl(String url) {
        this.url = url;
    }

    public void updateProfileUrl(UrlLabel label, String url) {
        this.label = label;
        this.url = url;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
