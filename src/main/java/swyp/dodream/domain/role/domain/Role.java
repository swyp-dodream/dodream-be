package swyp.dodream.domain.role.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.role.enums.RoleEnum;

@Entity
@Table(name = "profile_roles")
@Getter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    public Role(Long id, Profile profile, RoleEnum role) {
        this.id = id;
        this.profile = profile;
        this.role = role;
    }

    public Role(Profile profile, RoleEnum role) {
        this.profile = profile;
        this.role = role;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void updateRole(RoleEnum role) {
        this.role = role;
    }
}
