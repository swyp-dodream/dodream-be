package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

public interface MatchedRepository extends JpaRepository<Matched, Long> {

    /**
     * 무한 스크롤: 멤버 목록 조회 (최초 로드)
     */
    @Query("""
        SELECT m FROM Matched m
        JOIN FETCH m.user
        WHERE m.post.id = :postId
          AND m.canceled = false
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
          AND m.canceled = false
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
          AND m.canceled = false
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
          AND m.canceled = false
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
    boolean existsByPostAndUserAndCanceledFalse(Post post, User user);
}