package swyp.dodream.domain.bookmark.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndPost(User user, Post post);
    void deleteByUserAndPost(User user, Post post);

    @Query(
            value = """
            SELECT b
            FROM Bookmark b
            JOIN FETCH b.post p
            WHERE b.user = :user
              AND p.deleted = false
            """,
            countQuery = """
            SELECT count(b)
            FROM Bookmark b
            JOIN b.post p
            WHERE b.user = :user
              AND p.deleted = false
            """
    )
    Page<Bookmark> findAllByUser(@Param("user") User user, Pageable pageable);

    List<Bookmark> findByPost(Post post);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Page<Bookmark> findByUserId(Long userId, Pageable pageable);

    Page<Bookmark> findByUserIdAndPost_ProjectType(Long userId,ProjectType projectType,Pageable pageable);

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}