package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
