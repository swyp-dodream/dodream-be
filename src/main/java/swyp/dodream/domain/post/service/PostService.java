package swyp.dodream.domain.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.*;
import swyp.dodream.domain.post.dto.PostRequest;
import swyp.dodream.domain.post.dto.PostRoleDto;
import swyp.dodream.domain.post.dto.ApplicationRequest;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import swyp.dodream.domain.post.dto.PostUpdateRequest;
import swyp.dodream.domain.post.dto.MyPostListResponse;
import swyp.dodream.domain.post.dto.MyPostResponse;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.post.repository.*;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final SnowflakeIdService snowflakeIdService;
    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;
    private final UserRepository userRepository;
    private final PostStackRepository postStackRepository;
    private final PostRoleRepository postRoleRepository;
    private final PostFieldRepository postFieldRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchedRepository matchedRepository;  // 🔜 추가!

    // 모집글 생성
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 아닙니다."));

        // 비즈니스 검증 (필수 값 등)
        validatePostRequest(request);

        // 모집글 기본 정보 저장
        Post post = Post.builder()
                .id(snowflakeIdService.generateId())
                .owner(user)
                .projectType(request.getProjectType())
                .activityMode(request.getActivityMode())
                .duration(request.getDuration())
                .deadlineAt(request.getDeadlineAt())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.RECRUITING)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        postRepository.save(post);

        // PostView 생성
        PostView postView = new PostView();
        postView.setPost(post);
        postViewRepository.save(postView);

        // 스터디(STUDY)가 아닐 때만 관심 분야 연결
        connectFields(request, post);

        // 기술 스택 연결
        connectStacks(request, post);

        // 모집 직군 연결
        connectRoles(request, post);

        // 작성자를 Matched에 추가 (Application 없이)
        Matched ownerMatched = Matched.builder()
                .id(snowflakeIdService.generateId())
                .post(post)
                .user(user)
                .application(null)
                .matchedAt(LocalDateTime.now())
                .canceled(false)
                .build();

        matchedRepository.save(ownerMatched);

        boolean isOwner = post.getOwner().getId().equals(userId);
        return PostResponse.from(post, isOwner);
    }

    // 비즈니스 규칙 검증 메서드
    private void validatePostRequest(PostCreateRequest request) {

        // 공통 필수 값 확인
        if (request.getProjectType() == null)
            throw new IllegalArgumentException("프로젝트 유형은 필수입니다.");

        if (request.getActivityMode() == null)
            throw new IllegalArgumentException("활동 방식은 필수입니다.");

        if (request.getDuration() == null)
            throw new IllegalArgumentException("예상 활동 기간은 필수입니다.");

        if (request.getDeadlineAt() == null)
            throw new IllegalArgumentException("모집 마감일은 필수입니다.");

        if (request.getStackIds() == null || request.getStackIds().isEmpty())
            throw new IllegalArgumentException("기술 스택은 최소 1개 이상 선택해야 합니다.");

        if (request.getRoles() == null || request.getRoles().isEmpty())
            throw new IllegalArgumentException("모집 직군은 최소 1개 이상 선택해야 합니다.");

        // projectType에 따른 관심 분야 필수 여부
        if (request.getProjectType() == ProjectType.PROJECT) {
            if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
                throw new IllegalArgumentException("프로젝트는 관심 분야를 최소 1개 이상 선택해야 합니다.");
            }
        }
        // STUDY일 경우: 선택사항이라 비어있어도 허용
    }


    // 모집글 상세 조회
    @Transactional
    public PostResponse getPostDetail(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        // 조회수 조회 또는 생성
        PostView postView = postViewRepository.findById(postId)
                .orElseGet(() -> {
                    PostView newView = new PostView();
                    newView.setPost(post);
                    return postViewRepository.save(newView);
                });

        // 조회수 증가 및 저장
        post.increaseViewCount();
        postViewRepository.save(postView);

        // 자동 마감 처리
        if (post.getDeadlineAt() != null && post.getDeadlineAt().isBefore(LocalDateTime.now())) {
            post.closeRecruitment();
        }

        boolean isOwner = post.getOwner().getId().equals(userId);
        return PostResponse.from(post, isOwner);
    }

    // 모집글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 모집글을 수정할 수 있습니다.");
        }

        // 부분 수정 처리
        if (request.getTitle() != null && !request.getTitle().isBlank())
            post.updateTitle(request.getTitle());

        if (request.getContent() != null && !request.getContent().isBlank())
            post.updateContent(request.getContent());

        if (request.getActivityMode() != null)
            post.updateActivityMode(request.getActivityMode());

        if (request.getDuration() != null)
            post.updateDuration(request.getDuration());

        if (request.getDeadlineAt() != null)
            post.updateDeadlineAt(request.getDeadlineAt());

        if (request.getProjectType() != null)
            post.updateProjectType(request.getProjectType());

        if (request.getStatus() != null)
            post.updateStatus(request.getStatus());

        // 스택, 직군, 분야는 전달된 경우에만 갱신
        if (request.getStackIds() != null) {
            postStackRepository.deleteAllByPost(post);
            connectStacks(request, post);
        }

        if (request.getRoles() != null) {
            postRoleRepository.deleteAllByPost(post);
            connectRoles(request, post);
        }

        // STUDY가 아닐 때만 관심 분야 적용
        if (request.getProjectType() == null || request.getProjectType() == ProjectType.PROJECT) {
            if (request.getCategoryIds() != null) {
                postFieldRepository.deleteAllByPost(post);
                connectFields(request, post);
            }
        }

        boolean isOwner = post.getOwner().getId().equals(userId);
        return PostResponse.from(post, isOwner);
    }

    // 모집글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!post.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 모집글을 삭제할 수 있습니다.");
        }

        // 마지막으로 모집글 삭제
        postRepository.delete(post);
    }

    // 모집글 지원
    @Transactional
    public void applyToPost(Long postId, Long userId, ApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 아닙니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        // 리더(작성자)는 지원 불가
        if (post.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자는 자신의 모집글에 지원할 수 없습니다.");
        }

        if (post.getStatus() == PostStatus.COMPLETED)
            throw new IllegalStateException("모집이 마감되었습니다.");

        if (applicationRepository.existsByPostAndApplicant(post, user))
            throw new IllegalStateException("이미 지원한 모집글입니다.");

        Role role = new Role();
        role.setId(request.getRoleId());

        Application application = new Application(post, user, role, request.getMessage());
        applicationRepository.save(application);
    }

    // 모집글 지원 가능 여부 판단
    @Transactional(readOnly = true)
    public boolean canApply(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        // 리더는 지원 불가
        return !post.getOwner().getId().equals(userId);
    }


    private void connectRoles(PostRequest request, Post post) {
        if (request.getRoles() != null) {
            for (PostRoleDto roleDto : request.getRoles()) {
                Role role = new Role();
                role.setId(roleDto.getRoleId());
                PostRole pr = new PostRole(post, role, roleDto.getCount());
                postRoleRepository.save(pr);
            }
        }
    }

    private void connectStacks(PostRequest request, Post post) {
        if (request.getStackIds() != null) {
            for (Long stackId : request.getStackIds()) {
                TechSkill skill = new TechSkill();
                skill.setId(stackId);
                PostStack ps = new PostStack(post, skill);
                postStackRepository.save(ps);
            }
        }
    }

    private void connectFields(PostRequest request, Post post) {
        List<Long> categoryIds = request.getCategoryIds();

        // 카테고리가 아예 없으면 아무것도 안 함 (선택 사항)
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        // 관심 분야는 최대 2개까지만 선택 가능
        if (categoryIds.size() > 2) {
            throw new IllegalArgumentException("분야는 최대 2개까지만 선택할 수 있습니다.");
        }

        // projectType이 PROJECT이든 STUDY이든, categoryIds가 있으면 저장
        for (Long keywordId : categoryIds) {
            InterestKeyword keyword = new InterestKeyword();
            keyword.setId(keywordId);
            PostField pf = new PostField(post, keyword);
            postFieldRepository.save(pf);
        }
    }


    @Transactional(readOnly = true)
    public MyPostListResponse getMyPosts(Long userId, String tab, String status, Integer page, Integer size) {
        // 1. 사용자 검증
        userRepository.findByIdAndStatusTrue(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.UNAUTHORIZED, "탈퇴한 사용자입니다."));

        // 2. 페이징 설정
        Pageable pageable = PageRequest.of(page, size);

        // 3. 탭 파싱 (project/study만 가능, null이면 예외)
        ProjectType projectType;
        if (tab == null || tab.isBlank()) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID,
                    "탭 값은 필수입니다. (가능한 값: project, study)");
        }

        try {
            projectType = ProjectType.valueOf(tab.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID,
                    "유효하지 않은 탭 값입니다: " + tab + " (가능한 값: project, study)");
        }

        // 4. 상태 파싱 (recruiting/completed, 선택)
        PostStatus postStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                postStatus = PostStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ExceptionType.BAD_REQUEST_INVALID,
                        "유효하지 않은 상태 값입니다: " + status + " (가능한 값: recruiting, completed)");
            }
        }

        // 5. 조건에 따라 게시글 조회
        Page<Post> postsPage;
        if (postStatus != null) {
            // 탭 + 상태 둘 다 필터
            postsPage = postRepository.findMyPostsByProjectTypeAndStatus(userId, projectType, postStatus, pageable);
        } else {
            // 탭만 필터
            postsPage = postRepository.findMyPostsByProjectType(userId, projectType, pageable);
        }

        // 6. DTO 변환 (조회수 포함)
        Page<MyPostResponse> responsePage = postsPage.map(post -> {
            Long viewCount = postViewRepository.findById(post.getId())
                    .map(pv -> pv.getViews())
                    .orElse(0L);
            return MyPostResponse.from(post, viewCount);
        });

        // 7. 최종 응답 생성
        return MyPostListResponse.of(responsePage);
    }
}