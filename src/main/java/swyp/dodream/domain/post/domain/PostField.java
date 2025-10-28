package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "post_field")
@IdClass(PostFieldId.class)
public class PostField {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_keyword_id")
    private InterestKeyword interestKeyword;
}

