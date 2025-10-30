package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.domain.Application;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByPostAndApplicant(Post post, User applicant);

    // 정렬 기준에 AI 일단 넣지 말기
    /**
     * 무한 스크롤: 지원 목록 조회 (최초 로드)
     * 최신순 정렬
     */
    @Query("""
        SELECT a FROM Application a
        JOIN FETCH a.applicant
        WHERE a.post.id = :postId
        ORDER BY a.createdAt DESC
    """)
    Slice<Application> findApplicationsByPost(
            @Param("postId") Long postId,
            Pageable pageable
    );

    /**
     * 무한 스크롤: 지원 목록 조회 (다음 페이지)
     */
    @Query("""
        SELECT a FROM Application a
        JOIN FETCH a.applicant
        WHERE a.post.id = :postId
          AND a.id < :cursor
        ORDER BY a.createdAt DESC
    """)
    Slice<Application> findApplicationsByPostAfterCursor(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
