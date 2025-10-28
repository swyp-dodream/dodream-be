package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.master.domain.InterestKeyword;

@Entity
@NoArgsConstructor
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

    public PostField(Post post, InterestKeyword interestKeyword) {
        this.post = post;
        this.interestKeyword = interestKeyword;
    }
}

