package swyp.dodream.domain.matched.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchedRepository extends JpaRepository<Matched, Long> {

    /**
     * 무한 스크롤: 멤버 목록 조회 (최초 로드)
     */
    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.user
        WHERE m.post.id = :postId
          AND m.isCanceled = false 
        ORDER BY m.matchedAt DESC
    """)
    Slice<Matched> findMembersByPost(
            @Param("postId") Long postId,
            Pageable pageable
    );

    /**
     * 무한 스크롤: 멤버 목록 조회 (다음 페이지)
     */
    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.user
        WHERE m.post.id = :postId
          AND m.isCanceled = false 
          AND m.id < :cursor
        ORDER BY m.matchedAt DESC
    """)
    Slice<Matched> findMembersByPostAfterCursor(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 특정 게시글의 팀원인지 확인
     */
    boolean existsByPostAndUserAndIsCanceledFalse(Post post, User user);

    /**
     * 리더의 모집글 단위 매칭 취소 횟수 계산
     */
    @Query("""
        SELECT COUNT(m)
        FROM Matched m
        WHERE m.post.id = :postId
          AND m.isCanceled = true
          AND m.canceledBy = 'LEADER'
    """)
    int countLeaderCancelsForPost(@Param("postId") Long postId);

    /**
     * 멤버의 월 기준 매칭 취소 횟수 계산
     */
    @Query("""
        SELECT COUNT(m)
        FROM Matched m
        WHERE m.user.id = :memberUserId
          AND m.isCanceled = true
          AND m.canceledBy = 'MEMBER'
          AND m.canceledAt BETWEEN :start AND :end
    """)
    int countMemberCancelsInRange(
            @Param("memberUserId") Long memberUserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<Matched> findById(Long id);

    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.user
        WHERE m.post.id = :postId
          AND m.isCanceled = false
    """)
    List<Matched> findAllByPostId(@Param("postId") Long postId);

    @Query(value = """
    SELECT m
    FROM Matched m
    JOIN FETCH m.post p
    JOIN FETCH p.owner
    WHERE m.user.id = :userId
     AND m.isCanceled = false
    ORDER BY m.matchedAt DESC
    """,
            countQuery = """
    SELECT count(m)
    FROM Matched m
    WHERE m.user.id = :userId
    """
    )
    Page<Matched> findMatchedByUser(@Param("userId") Long userId, Pageable pageable);

    boolean existsByPostIdAndUserId(Long postId, Long id);
}
