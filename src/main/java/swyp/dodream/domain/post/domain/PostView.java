package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "post_view")
public class PostView {

    @Id
    private Long postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private Long views = 0L;

    @Column(nullable = false)
    private Boolean deleted = false;

    public void increment() {
        this.views++;
    }
}