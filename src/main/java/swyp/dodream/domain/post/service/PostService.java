package swyp.dodream.domain.post.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.application.dto.request.ApplicationRequest;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.common.PostSortType;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.*;
import swyp.dodream.domain.post.dto.request.PostCreateRequest;
import swyp.dodream.domain.post.dto.request.PostRequest;
import swyp.dodream.domain.post.dto.PostRoleDto;
import swyp.dodream.domain.post.dto.request.PostUpdateRequest;
import swyp.dodream.domain.post.dto.response.MyPostListResponse;
import swyp.dodream.domain.post.dto.response.MyPostResponse;
import swyp.dodream.domain.post.dto.response.PostResponse;
import swyp.dodream.domain.post.repository.*;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.search.document.PostDocument;
import swyp.dodream.domain.search.repository.PostDocumentRepository;
import swyp.dodream.domain.suggestion.repository.SuggestionRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;
import swyp.dodream.domain.ai.service.EmbeddingService;
import swyp.dodream.domain.recommendation.repository.VectorRepository;
import swyp.dodream.domain.recommendation.util.TextExtractor;
import swyp.dodream.domain.master.repository.RoleRepository;
import swyp.dodream.domain.master.repository.TechSkillRepository;
import swyp.dodream.domain.master.repository.InterestKeywordRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final MatchedRepository matchedRepository;
    private final RoleRepository roleRepository;
    private final TechSkillRepository techSkillRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final EntityManager entityManager;
    private final PostDocumentRepository postDocumentRepository;
    private final SuggestionRepository suggestionRepository;
    private final NotificationService notificationService;
    private final ProfileRepository profileRepository;

    // 벡터 임베딩 관련 (옵션) - NCP 배포 시에만 활성화
    private final Optional<EmbeddingService> embeddingService;
    private final Optional<VectorRepository> vectorRepository;

    // 모집글 생성
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(ExceptionType.USER_NOT_FOUND::throwException);

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

        postRepository.save(post);  // Post 먼저 저장 (FK 참조 위해 필요)
        postRepository.flush();  // Post를 DB에 즉시 반영 (PostView 저장 전 세션 정리)

        // 스터디(STUDY)가 아닐 때만 관심 분야 연결
        connectFields(request, post);

        // 기술 스택 연결
        connectStacks(request, post);

        // 모집 직군 연결
        connectRoles(request, post);

        // PostView 생성 및 연결 (OneToOne 양방향 연결)
        PostView postView = new PostView();
        postView.setPost(entityManager.getReference(Post.class, post.getId()));  // detached reference 사용
        postViewRepository.save(postView);  // PostView 저장 (FK 연결)

        // 작성자를 Matched에 추가 (Application 없이)
        Matched ownerMatched = Matched.builder()
                .id(snowflakeIdService.generateId())
                .post(post)
                .user(user)
                .application(null)
                .matchedAt(LocalDateTime.now())
                .isCanceled(false)
                .build();

        matchedRepository.save(ownerMatched);
        entityManager.flush();

        // 게시글 임베딩 생성 (Qdrant에 저장)
        createPostEmbeddingAsync(post);

        postDocumentRepository.save(
                PostDocument.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getContent())
                        .build()
        );

        return buildPostResponse(post, userId);
    }

    /**
     * 게시글 임베딩 생성 및 벡터 DB 저장 (비동기)
     * 실패해도 게시글 생성에는 영향 없음
     */
    private void createPostEmbeddingAsync(Post post) {
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            // 벡터 DB 미사용 환경
            return;
        }

        try {
            // 게시글 → 텍스트 → 임베딩 생성
            String postText = TextExtractor.extractFromPost(post);
            float[] embedding = embeddingService.get().embed(postText);

            // payload 생성 (메타데이터)
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", post.getTitle());
            payload.put("content", post.getContent());
            payload.put("projectType", post.getProjectType().name());
            payload.put("activityMode", post.getActivityMode().name());

            // Qdrant에 저장 (payload 포함)
            vectorRepository.get().upsertVector(post.getId(), embedding, payload);

        } catch (Exception e) {
            // 임베딩 실패 시 로깅만 (게시글 생성은 정상 완료)
            // TODO: 로깅
        }
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
            if (request.getInterestIds() == null || request.getInterestIds().isEmpty()) {
                throw new IllegalArgumentException("프로젝트는 관심 분야를 최소 1개 이상 선택해야 합니다.");
            }
        }
        // STUDY일 경우: 선택사항이라 비어있어도 허용
    }


    // 모집글 상세 조회
    @Transactional
    public PostResponse getPostDetail(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

        // 조회수 조회 또는 생성
        PostView postView = postViewRepository.findById(postId)
                .orElseGet(() -> {
                    PostView newView = new PostView();
                    newView.setPost(entityManager.getReference(Post.class, post.getId())); // ★ 세션 충돌/식별자 중복 방지
                    return postViewRepository.save(newView);
                });

        // 조회수 증가 및 저장
        post.increaseViewCount();
        postViewRepository.save(postView);

        // 자동 마감 처리
        if (post.getDeadlineAt() != null && post.getDeadlineAt().isBefore(LocalDateTime.now())) {
            post.closeRecruitment();
        }

        return buildPostResponse(post, userId);
    }

    // 모집글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

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
            if (request.getInterestIds() != null) {
                postFieldRepository.deleteAllByPost(post);
                connectFields(request, post);
            }
        }

        // 게시글 업데이트 시 임베딩 재생성
        createPostEmbeddingAsync(post);

        postDocumentRepository.save(
                PostDocument.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getContent())
                        .build()
        );

        return buildPostResponse(post, userId);
    }

    // 모집글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

        // 작성자 본인만 삭제 가능
        if (!post.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("작성자만 모집글을 삭제할 수 있습니다.");
        }

        // Qdrant에서 벡터 삭제
        if (vectorRepository.isPresent()) {
            try {
                vectorRepository.get().deleteVector(postId);
            } catch (Exception e) {
                // 벡터 삭제 실패는 무시 (이미 DB 삭제됨)
            }
        }

        // 마지막으로 모집글 삭제
        postRepository.delete(post);
        postRepository.flush();

        postDocumentRepository.save(
                PostDocument.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getContent())
                        .build()
        );
    }

    @Transactional
    public void applyToPost(Long postId, Long userId, ApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(ExceptionType.USER_NOT_FOUND::throwException);

        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

        // 0. 자기 글은 지원 불가
        if (post.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자는 자신의 모집글에 지원할 수 없습니다.");
        }

        // 1. 모집 마감 여부
        if (post.getStatus() == PostStatus.COMPLETED) {
            throw new IllegalStateException("모집이 마감되었습니다.");
        }

        // 2. roleId(String) -> Long 변환 + 직군 엔티티 조회 (신규/재지원 공통에 사용)
        Long roleId;
        try {
            roleId = Long.valueOf(request.getRoleId());
        } catch (NumberFormatException e) {
            throw new CustomException(
                    ExceptionType.BAD_REQUEST_INVALID,
                    "roleId는 숫자 형식이어야 합니다. 입력값: " + request.getRoleId()
            );
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(ExceptionType.ROLE_NOT_FOUND::throwException);

        // 3. 기존 지원 이력 조회
        Optional<Application> existingAppOpt =
                applicationRepository.findByPostIdAndApplicantId(postId, userId);

        if (existingAppOpt.isPresent()) {
            Application existingApp = existingAppOpt.get();

            // 이미 활성(APPLIED) 상태면 재지원 불가
            if (existingApp.getStatus().isActive()) {
                throw new IllegalStateException("이미 지원한 모집글입니다.");
            }

            // WITHDRAWN / REJECTED 등 → 재지원 처리
            existingApp.updateReapply(role, request.getMessage());
            return;
        }

        // 4. 기존 이력이 없으면 신규 지원
        Long applicationId = snowflakeIdService.generateId();
        Application application = new Application(
                applicationId,
                post,
                user,
                role,
                request.getMessage()
        );
        applicationRepository.save(application);

        // 5. 제안 알림 로직 기존 그대로
        Long leaderId = post.getOwner().getId();
        Long applicantId = user.getId();

        suggestionRepository.findLatestValidSuggestion(postId, leaderId, applicantId)
                .ifPresent(suggestion -> {
                    notificationService.sendProposalAppliedNotification(
                            leaderId,
                            postId,
                            user.getName(),
                            post.getTitle()
                    );
                });
    }

    // 모집글 지원 가능 여부 판단
    @Transactional(readOnly = true)
    public boolean canApply(Long postId, Long userId) {
        // 1. 로그인 여부
        if (userId == null) {
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

        // 2. 리더(작성자) 본인 지원 불가
        if (post.getOwner().getId().equals(userId)) {
            return false;
        }

        // 3. 모집이 이미 완료된 경우 지원 불가
        if (post.getStatus() == PostStatus.COMPLETED) {
            return false;
        }

        // 4. 이미 활성 지원(APPLIED)이 존재하면 지원 불가
        boolean hasActiveApplication = applicationRepository
                .findByPostIdAndApplicantId(postId, userId)
                .filter(app -> app.getStatus().isActive())
                .isPresent();

        return !hasActiveApplication;
    }


    private void connectRoles(PostRequest request, Post post) {
        if (request.getRoles() != null) {
            for (PostRoleDto roleDto : request.getRoles()) {
                Role role = roleRepository.findById(roleDto.getRoleId())
                        .orElseThrow(ExceptionType.ROLE_NOT_FOUND::throwException);
                PostRole pr = new PostRole(snowflakeIdService.generateId(), post, role, roleDto.getCount());
                postRoleRepository.save(pr);
            }
        }
    }

    private void connectStacks(PostRequest request, Post post) {
        if (request.getStackIds() != null) {
            for (Long stackId : request.getStackIds()) {
                TechSkill skill = techSkillRepository.findById(stackId)
                        .orElseThrow(ExceptionType.TECH_SKILL_NOT_FOUND::throwException);
                PostStack ps = new PostStack(post, skill);
                postStackRepository.save(ps);
            }
        }
    }

    private void connectFields(PostRequest request, Post post) {
        if (request.getInterestIds() != null) {
            for (Long keywordId : request.getInterestIds()) {
                InterestKeyword keyword = interestKeywordRepository.findById(keywordId)
                        .orElseThrow(ExceptionType.INTEREST_NOT_FOUND::throwException);
                PostField pf = new PostField(post, keyword);
                postFieldRepository.save(pf);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(PostSortType sortType, ProjectType projectType, Pageable pageable) {
        Sort sort;

        switch (sortType) {
            case DEADLINE:
                sort = Sort.by(Sort.Direction.ASC, "deadlineAt"); // 마감이 가까운 순
                break;
            case POPULAR:
                sort = Sort.by(Sort.Direction.DESC, "postView.views"); // 인기순 (조회수 많은 순)
                break;
            case LATEST:
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
                break;
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Specification<Post> spec = Specification.where(PostSpecification.notDeleted())
                                    .and(PostSpecification.hasType(projectType));

        return postRepository.findAll(spec, sortedPageable)
                .map(post -> buildPostResponse(post, null)); // 목록: isOwner=false, 프로필 정보는 포함
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

        if (status != null && status.isBlank()) status = null;

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

    private PostResponse buildPostResponse(Post post, Long currentUserId) {
        boolean isOwner = currentUserId != null && post.getOwner().getId().equals(currentUserId);

        Profile profile = profileRepository.findByUserId(post.getOwner().getId())
                .orElse(null);

        String ownerNickname = profile != null ? profile.getNickname() : null;

        Integer profileImageCode = profile != null ? profile.getProfileImageCode() : null;
        String ownerProfileImageUrl = profileImageCode != null
                ? profileImageCode.toString()
                : null;

        Long applicationId = null;

        if (currentUserId != null && !isOwner) {
            applicationId = applicationRepository
                    .findByPostIdAndApplicantIdAndStatus(
                            post.getId(),
                            currentUserId,
                            ApplicationStatus.APPLIED)
                    .map(Application::getId)
                    .orElse(null);
        }

        return PostResponse.from(
                post,
                isOwner,
                ownerNickname,
                ownerProfileImageUrl,
                applicationId
        );
    }
}