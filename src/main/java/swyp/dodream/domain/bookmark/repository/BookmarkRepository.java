package swyp.dodream.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndPost(User user, Post post);
    void deleteByUserAndPost(User user, Post post);
    List<Bookmark> findAllByUser(User user);
}
