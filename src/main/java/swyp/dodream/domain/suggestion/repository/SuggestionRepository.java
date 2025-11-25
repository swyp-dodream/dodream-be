package swyp.dodream.domain.suggestion.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.master.domain.SuggestionStatus;
import swyp.dodream.domain.suggestion.domain.Suggestion;

import java.util.List;
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

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Suggestion s
        WHERE s.post.id = :postId
          AND s.toUser.id = :toUserId
          AND s.withdrawnAt IS NULL
          AND s.status = 'SENT'
    """)
    boolean existsActiveByPostIdAndToUserId(@Param("postId") Long postId,
                                            @Param("toUserId") Long toUserId);

    @Query("""
        SELECT s
        FROM Suggestion s
        WHERE s.post.id = :postId
          AND s.toUser.id = :toUserId
          AND s.withdrawnAt IS NULL
          AND s.status = swyp.dodream.domain.master.domain.SuggestionStatus.SENT
    """)
    Optional<Suggestion> findActiveByPostIdAndToUserId(@Param("postId") Long postId,
                                                       @Param("toUserId") Long toUserId);

    @Query("""
    SELECT s.id
    FROM Suggestion s
    WHERE s.post.id = :postId
      AND s.toUser.id = :userId
      AND s.status IN :validStatuses
    """)
    Optional<Long> findValidSuggestionId(@Param("postId") Long postId, @Param("userId") Long userId, @Param("validStatuses") List<SuggestionStatus> validStatuses);

    Optional<Suggestion> findByPostIdAndToUserId(Long postId, Long toUserId);

    @Query("""
    SELECT s
    FROM Suggestion s
    WHERE s.toUser.id = :userId
      AND NOT EXISTS (
          SELECT 1 FROM Application a
          WHERE a.post.id = s.post.id
            AND a.applicant.id = :userId
            AND (a.status = swyp.dodream.domain.master.domain.ApplicationStatus.APPLIED
                 OR a.status = swyp.dodream.domain.master.domain.ApplicationStatus.ACCEPTED)
      )
    """)
    Page<Suggestion> findSuggestionsExcludingApplied(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT s
    FROM Suggestion s
    WHERE s.post.id = :postId
      AND s.toUser.id IN :toUserIds
      AND s.withdrawnAt IS NULL
      AND s.status = swyp.dodream.domain.master.domain.SuggestionStatus.SENT
""")
    List<Suggestion> findActiveByPostIdAndToUserIdIn(
            @Param("postId") Long postId,
            @Param("toUserIds") List<Long> toUserIds
    );
}
