package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.PostRole;

public interface PostRoleRepository extends JpaRepository<PostRole, Long> {
    void deleteAllByPost(Post post);
}

