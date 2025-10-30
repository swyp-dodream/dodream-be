package swyp.dodream.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.post.domain.Application;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByPostAndApplicant(Post post, User applicant);
}
