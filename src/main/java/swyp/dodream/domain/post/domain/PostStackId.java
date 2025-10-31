package swyp.dodream.domain.post.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostStackId implements Serializable {
    private Long post;        // Post의 PK 값
    private Long techSkill;   // TechSkill의 PK 값

    public PostStackId() {}

    public PostStackId(Long post, Long techSkill) {
        this.post = post;
        this.techSkill = techSkill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostStackId)) return false;
        PostStackId that = (PostStackId) o;
        return Objects.equals(post, that.post)
                && Objects.equals(techSkill, that.techSkill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, techSkill);
    }
}
