package swyp.dodream.domain.techstack.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.techstack.enums.TechStackEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_tech_stacks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class TechStack {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechStackEnum techStack;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TechStack(Profile profile, TechStackEnum techStack) {
        this.profile = profile;
        this.techStack = techStack;
    }

    public TechStack(Long id, Profile profile, TechStackEnum techStack) {
        this.id = id;
        this.profile = profile;
        this.techStack = techStack;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
