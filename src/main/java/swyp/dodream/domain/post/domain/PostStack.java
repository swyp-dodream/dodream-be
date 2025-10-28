package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "post_stack")
//@IdClass(PostStackId.class)
public class PostStack {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    @Id
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tech_skill_id")
//    private TechSkill techSkill;
}
