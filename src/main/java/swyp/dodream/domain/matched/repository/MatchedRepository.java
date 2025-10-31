package swyp.dodream.domain.matched.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
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
     * 특정 유저가 매칭된 모집글 목록 조회
     *
     * @param userId 유저 ID
     * @param pageable 페이징 정보
     * @return 매칭된 모집글 목록
     */
    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.post p
        JOIN FETCH p.owner
        WHERE m.user.id = :userId
          AND m.isCanceled = false
        ORDER BY m.matchedAt DESC
    """)
    Slice<Matched> findMatchedByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 특정 유저가 매칭된 모집글 목록 조회 (커서 기반)
     */
    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.post p
        JOIN FETCH p.owner
        WHERE m.user.id = :userId
          AND m.isCanceled = false
          AND m.id < :cursor
        ORDER BY m.matchedAt DESC
    """)
    Slice<Matched> findMatchedByUserAfterCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 특정 게시글의 팀원인지 확인
     */
    boolean existsByPostAndUserAndIsCanceledFalse(Post post, User user);

    /**
     * 리더의 모집글 단위 매칭 취소 횟수 계산
     * - 모집글(post_id) 기준으로 leader가 취소한 매칭 수를 센다.
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
     * - 특정 유저(user_id)가 'MEMBER'로 매칭 취소한 건수를 기간 내 집계한다.
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

    /**
     * 매칭 단건 조회
     * - 서비스에서 취소 주체(리더/멤버) 판별용으로 사용.
     */
    Optional<Matched> findById(Long id);
}