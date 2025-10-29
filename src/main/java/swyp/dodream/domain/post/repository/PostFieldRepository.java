package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.PostField;
import swyp.dodream.domain.post.domain.PostFieldId;

public interface PostFieldRepository extends JpaRepository<PostField, PostFieldId> {
    void deleteAllByPost(Post post);
}
