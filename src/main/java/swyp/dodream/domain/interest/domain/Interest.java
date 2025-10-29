package swyp.dodream.domain.interest.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.interest.enums.InterestEnum;
import swyp.dodream.domain.profile.domain.Profile;

@Entity
@Table(name = "profile_interests")
@Getter
@NoArgsConstructor
public class Interest extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestEnum interest;

    public Interest(Long id, Profile profile, InterestEnum interest) {
        this.id = id;
        this.profile = profile;
        this.interest = interest;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
