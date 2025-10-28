package swyp.dodream.domain.interest.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.domain.interest.enums.InterestEnum;
import swyp.dodream.domain.profile.domain.Profile;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_interests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Interest {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestEnum interest;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Interest(Long id, Profile profile, InterestEnum interest) {
        this.id = id;
        this.profile = profile;
        this.interest = interest;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
