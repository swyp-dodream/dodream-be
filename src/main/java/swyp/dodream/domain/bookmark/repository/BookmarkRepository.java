package swyp.dodream.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndPost(User user, Post post);
    void deleteByUserAndPost(User user, Post post);

    @Query("""
        SELECT b FROM Bookmark b
        JOIN FETCH b.post p
        WHERE b.user = :user
          AND p.deleted = false
    """)
    List<Bookmark> findAllByUser(@Param("user") User user);

    List<Bookmark> findByPost(Post post);
}
