package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.domain.Suggestion;

import java.util.Optional;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    /**
     * 무한 스크롤: 제안 목록 조회 (최초 로드)
     * → id 기준 최신순
     */
    @Query("""
        SELECT s FROM Suggestion s
        JOIN FETCH s.toUser
        JOIN FETCH s.post p
        WHERE s.post.id = :postId
          AND s.fromUser.id = :fromUserId
          AND s.withdrawnAt IS NULL
          AND p.deleted = false
        ORDER BY s.id DESC
    """)
    Slice<Suggestion> findSuggestionsByPost(
            @Param("postId") Long postId,
            @Param("fromUserId") Long fromUserId,
            Pageable pageable
    );

    /**
     * 무한 스크롤: 제안 목록 조회 (다음 페이지)
     * → id 커서로 다음 페이지
     */
    @Query("""
        SELECT s FROM Suggestion s
        JOIN FETCH s.toUser
        JOIN FETCH s.post p
        WHERE s.post.id = :postId
          AND s.fromUser.id = :fromUserId
          AND s.id < :cursor
          AND s.withdrawnAt IS NULL
          AND p.deleted = false
        ORDER BY s.id DESC
    """)
    Slice<Suggestion> findSuggestionsByPostAfterCursor(
            @Param("postId") Long postId,
            @Param("fromUserId") Long fromUserId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 내가 제안받은 모집글 목록 조회 (최초 로드)
     */
    @Query("""
        SELECT s FROM Suggestion s
        JOIN FETCH s.post p
        JOIN FETCH p.owner
        WHERE s.toUser.id = :userId
          AND s.withdrawnAt IS NULL
          AND p.deleted = false
        ORDER BY s.id DESC
    """)
    Slice<Suggestion> findSuggestionsByToUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 내가 제안받은 모집글 목록 조회 (커서 기반)
     */
    @Query("""
        SELECT s FROM Suggestion s
        JOIN FETCH s.post p
        JOIN FETCH p.owner
        WHERE s.toUser.id = :userId
          AND s.id < :cursor
          AND s.withdrawnAt IS NULL
          AND p.deleted = false
        ORDER BY s.id DESC
    """)
    Slice<Suggestion> findSuggestionsByToUserAfterCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
    SELECT s FROM Suggestion s
    WHERE s.post.id = :postId
      AND s.fromUser.id = :fromUserId
      AND s.toUser.id = :toUserId
      AND s.withdrawnAt IS NULL
    ORDER BY s.createdAt DESC
""")
    Optional<Suggestion> findLatestValidSuggestion(
            @Param("postId") Long postId,
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    boolean existsByPostIdAndToUserId(Long postId, Long toUserId);
}
