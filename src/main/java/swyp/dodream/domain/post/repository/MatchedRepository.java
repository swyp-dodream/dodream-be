package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.domain.Matched;

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
}