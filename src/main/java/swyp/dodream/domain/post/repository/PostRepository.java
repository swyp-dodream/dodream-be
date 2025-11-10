package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    /**
     * 내가 쓴 글 - 프로젝트 타입별 조회
     *
     * @param userId 사용자 ID
     * @param projectType 프로젝트 타입 (PROJECT or STUDY)
     * @param pageable 페이징 정보
     * @return 페이징된 게시글 목록
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.owner.id = :userId
          AND p.projectType = :projectType
          AND p.deleted = false
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findMyPostsByProjectType(
            @Param("userId") Long userId,
            @Param("projectType") ProjectType projectType,
            Pageable pageable
    );

    /**
     * 내가 쓴 글 - 프로젝트 타입 + 모집 상태 조합 조회
     *
     * @param userId 사용자 ID
     * @param projectType 프로젝트 타입 (PROJECT or STUDY)
     * @param status 모집 상태 (RECRUITING or COMPLETED)
     * @param pageable 페이징 정보
     * @return 페이징된 게시글 목록
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.owner.id = :userId
          AND p.projectType = :projectType
          AND p.status = :status
          AND p.deleted = false
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findMyPostsByProjectTypeAndStatus(
            @Param("userId") Long userId,
            @Param("projectType") ProjectType projectType,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    /**
     * 모집글의 리더(작성자) ID 조회
     * - 매칭 취소 시, 리더/멤버 판별을 위해 사용.
     */
    @Query("SELECT p.owner.id FROM Post p WHERE p.id = :postId AND p.deleted = false")
    Optional<Long> findOwnerUserIdByPostId(@Param("postId") Long postId);

    // 마감일 기반 알림 스케줄러에서 사용
    List<Post> findByDeadlineAtBetween(LocalDateTime start, LocalDateTime end);
}