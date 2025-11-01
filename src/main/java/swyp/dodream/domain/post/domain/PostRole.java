package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.master.domain.Role;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "post_role_requirement")
public class PostRole {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private int headcount;

    public PostRole(Long id, Post post, Role role, int headcount) {
        this.id = id;
        this.post = post;
        this.role = role;
        this.headcount = headcount;
    }
}
