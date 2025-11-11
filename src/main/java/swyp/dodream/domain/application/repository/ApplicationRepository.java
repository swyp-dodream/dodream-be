package swyp.dodream.domain.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.util.Collection;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 특정 게시글에 특정 유저가 지원했는지 확인
     */
    boolean existsByPostAndApplicant(Post post, User applicant);

    Optional<Application> findByIdAndApplicantId(Long id, Long applicantId);

    boolean existsByPostIdAndApplicantIdAndStatusIn(
            Long postId, Long applicantId, Collection<ApplicationStatus> statuses
    );

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

    @Query("""
    SELECT a FROM Application a
    JOIN FETCH a.applicant
    WHERE a.id = :applicationId
      AND a.post.id = :postId
""")
    Optional<Application> findByIdAndPostId(@Param("applicationId") Long applicationId,
                                            @Param("postId") Long postId);

    @Query(value = """
        SELECT a
        FROM Application a
        JOIN FETCH a.post p
        JOIN FETCH p.owner
        WHERE a.applicant.id = :userId
        ORDER BY a.createdAt DESC
        """,
            countQuery = """
        SELECT count(a)
        FROM Application a
        WHERE a.applicant.id = :userId
        """
    )
    Page<Application> findApplicationsByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
