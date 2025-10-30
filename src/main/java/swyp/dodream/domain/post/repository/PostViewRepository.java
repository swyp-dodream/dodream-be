package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.post.domain.PostView;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
}
