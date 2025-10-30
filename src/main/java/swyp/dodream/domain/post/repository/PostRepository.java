package swyp.dodream.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

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
}