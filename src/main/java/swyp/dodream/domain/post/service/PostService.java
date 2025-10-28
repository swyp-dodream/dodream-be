package swyp.dodream.domain.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.Role;
import swyp.dodream.domain.master.TechSkill;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.PostRole;
import swyp.dodream.domain.post.domain.PostStack;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.post.dto.PostRoleDto;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.PostRoleRepository;
import swyp.dodream.domain.post.repository.PostStackRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import swyp.dodream.domain.master.InterestKeyword;
import swyp.dodream.domain.post.domain.PostField;
import swyp.dodream.domain.post.repository.PostFieldRepository;

@Service
@RequiredArgsConstructor
public class PostService {
    private final SnowflakeIdService snowflakeIdService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostStackRepository postStackRepository;
    private final PostRoleRepository postRoleRepository;
    private final PostFieldRepository postFieldRepository;


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

        // 분야(InterestKeyword) 연결
        if (request.getCategoryIds() != null) {
            for (Long keywordId : request.getCategoryIds()) {
                InterestKeyword keyword = new InterestKeyword();
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

        if (post.getStatus() == PostStatus.CLOSED)
            throw new IllegalStateException("모집이 마감되었습니다.");

        if (applicationRepository.existsByPostAndApplicant(post, user))
            throw new IllegalStateException("이미 지원한 모집글입니다.");

        Application application = new Application(post, user, request.getMessage());
        applicationRepository.save(application);
    }

}

