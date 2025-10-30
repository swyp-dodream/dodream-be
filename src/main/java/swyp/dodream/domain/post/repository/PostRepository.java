package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import swyp.dodream.domain.post.domain.Post;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
}
