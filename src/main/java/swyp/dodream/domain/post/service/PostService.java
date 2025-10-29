package swyp.dodream.domain.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.*;
import swyp.dodream.domain.post.dto.*;
import swyp.dodream.domain.post.repository.*;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import swyp.dodream.domain.master.domain.InterestKeyword;

@Service
@RequiredArgsConstructor
public class PostService {
    private final SnowflakeIdService snowflakeIdService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostStackRepository postStackRepository;
    private final PostRoleRepository postRoleRepository;
    private final PostFieldRepository postFieldRepository;
    private final ApplicationRepository applicationRepository;

    // 홈 목록 조회 (필터 + 검색 + 페이지네이션)
    public Page<PostSummaryResponse> getHomePosts(
            ProjectType type,
            String keyword,
            String role,
            String tech,
            String interest,
            ActivityMode activityMode,
            boolean onlyRecruiting,
            String sort,
            Pageable pageable
    ) {
        Specification<Post> spec = Specification.where(PostSpecification.hasType(type));

        if (keyword != null && !keyword.isBlank())
            spec = spec.and(PostSpecification.containsKeyword(keyword));

        if (role != null)
            spec = spec.and(PostSpecification.hasRole(role));

        if (tech != null)
            spec = spec.and(PostSpecification.hasTech(tech));

        // 스터디(STUDY)인 경우에는 interest 필터를 완전히 무시
        if (type != ProjectType.STUDY && interest != null)
            spec = spec.and(PostSpecification.hasInterest(interest));

        if (activityMode != null)
            spec = spec.and(PostSpecification.hasActivityMode(activityMode));

        if (onlyRecruiting)
            spec = spec.and(PostSpecification.onlyRecruiting());

        Sort sorting = switch (sort) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "recruitEndDate");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Page<Post> posts = postRepository.findAll(
                spec,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting)
        );

        return posts.map(PostSummaryResponse::fromEntity);
    }

    // 모집글 생성
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 모집글 기본 정보 저장
        Post post = Post.builder()
                .id(snowflakeIdService.generateId())
                .owner(user)
                .projectType(request.getProjectType())
                .activityMode(request.getActivityMode())
                .durationText(request.getDurationText())
                .deadlineAt(request.getDeadlineAt())
                .status(PostStatus.RECRUITING)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        postRepository.save(post);

        // 스터디(STUDY)가 아닐 때만 관심 분야 연결
        if (request.getProjectType() != ProjectType.STUDY && request.getCategoryIds() != null) {
            for (Long keywordId : request.getCategoryIds()) {
                InterestKeyword keyword = new InterestKeyword();
                keyword.setId(snowflakeIdService.generateId());
                PostField pf = new PostField(post, keyword);
                postFieldRepository.save(pf);
            }
        }

        // 기술 스택 연결
        if (request.getStackIds() != null) {
            for (Long stackId : request.getStackIds()) {
                TechSkill skill = new TechSkill();
                skill.setId(stackId);
                PostStack ps = new PostStack(post, skill);
                postStackRepository.save(ps);
            }
        }

        // 모집 직군 연결
        if (request.getRoles() != null) {
            for (PostRoleDto roleDto : request.getRoles()) {
                Role role = new Role();
                role.setId(roleDto.getRoleId());
                PostRole pr = new PostRole(post, role, roleDto.getCount());
                postRoleRepository.save(pr);
            }
        }

        boolean isOwner = post.getOwner().getId().equals(userId);
        return PostResponse.from(post, isOwner);
    }

    // 모집글 상세 조회
    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        // 조회수 증가 (자신 포함)
        post.increaseViewCount();

        // 자동 마감 처리
        if (post.getDeadlineAt() != null && post.getDeadlineAt().isBefore(LocalDateTime.now())) {
            post.closeRecruitment();
        }

        boolean isOwner = post.getOwner().getId().equals(userId);
        return PostResponse.from(post, isOwner);
    }

    // 모집글 지원
    @Transactional
    public void applyToPost(Long postId, Long userId, ApplicationRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (post.getStatus() == PostStatus.COMPLETED)
            throw new IllegalStateException("모집이 마감되었습니다.");

        if (applicationRepository.existsByPostAndApplicant(post, user))
            throw new IllegalStateException("이미 지원한 모집글입니다.");

        Role role = new Role();
        role.setId(request.getRoleId());

        Application application = new Application(post, user, role, request.getMessage());
        applicationRepository.save(application);
    }
}

