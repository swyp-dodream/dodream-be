package swyp.dodream.domain.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.feedback.domain.Feedback;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 특정 유저가 특정 게시글에서 특정 유저에게 이미 피드백을 작성했는지 확인
     *
     * @param postId 게시글 ID
     * @param fromUserId 작성자 ID
     * @param toUserId 받는 사람 ID
     * @return 존재 여부
     */
    @Query("""
        SELECT COUNT(f) > 0 FROM Feedback f
        WHERE f.post.id = :postId
          AND f.fromUser.id = :fromUserId
          AND f.toUser.id = :toUserId
    """)
    boolean existsByPostAndFromUserAndToUser(
            @Param("postId") Long postId,
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    /**
     * 특정 유저가 특정 게시글에서 작성한 모든 피드백 조회
     * (내가 어떤 게시글에서 누구에게 피드백을 작성했는지)
     *
     * @param postId 게시글 ID
     * @param fromUserId 작성자 ID
     * @return 피드백 목록
     */
    @Query("""
        SELECT f FROM Feedback f
        JOIN FETCH f.toUser
        WHERE f.post.id = :postId
          AND f.fromUser.id = :fromUserId
        ORDER BY f.createdAt DESC
    """)
    List<Feedback> findByPostAndFromUser(
            @Param("postId") Long postId,
            @Param("fromUserId") Long fromUserId
    );

    /**
     * 특정 유저가 받은 모든 피드백 조회 (익명)
     * (내가 받은 피드백들 - 작성자는 익명)
     *
     * @param toUserId 받는 사람 ID
     * @return 피드백 목록
     */
    @Query("""
        SELECT f FROM Feedback f
        JOIN FETCH f.post p
        WHERE f.toUser.id = :toUserId
        ORDER BY f.createdAt DESC
    """)
    List<Feedback> findByToUser(@Param("toUserId") Long toUserId);

    /**
     * 특정 게시글의 모든 피드백 조회
     * (특정 프로젝트에서 주고받은 모든 피드백)
     *
     * @param postId 게시글 ID
     * @return 피드백 목록
     */
    @Query("""
        SELECT f FROM Feedback f
        JOIN FETCH f.fromUser
        JOIN FETCH f.toUser
        WHERE f.post.id = :postId
        ORDER BY f.createdAt DESC
    """)
    List<Feedback> findByPost(@Param("postId") Long postId);

    /**
     * 특정 유저가 특정 게시글에서 받은 피드백 개수
     * (통계용)
     *
     * @param postId 게시글 ID
     * @param toUserId 받는 사람 ID
     * @return 피드백 개수
     */
    @Query("""
        SELECT COUNT(f) FROM Feedback f
        WHERE f.post.id = :postId
          AND f.toUser.id = :toUserId
    """)
    long countByPostAndToUser(
            @Param("postId") Long postId,
            @Param("toUserId") Long toUserId
    );

    /**
     * 특정 게시글에서 특정 유저가 받은 피드백 조회
     */
    List<Feedback> findByPostAndToUserOrderByCreatedAtDesc(Post post, User toUser);

}