package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.master.domain.TechSkill;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "post_stack")
@IdClass(PostStackId.class)
public class PostStack {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_skill_id")
    private TechSkill techSkill;

    public PostStack(Post post, TechSkill techSkill) {
        this.post = post;
        this.techSkill = techSkill;
    }
}
