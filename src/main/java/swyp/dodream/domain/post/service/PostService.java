package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.post.dto.PostCreateRequest;
import swyp.dodream.domain.user.domain.User;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostResponse createPost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Post post = Post.builder()
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
        return PostResponse.from(post);
    }

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

    @PostMapping("/{postId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> applyToPost(@PathVariable Long postId,
                                            @AuthenticationPrincipal UserPrincipal user,
                                            @RequestBody(required = false) ApplicationRequest request) {
        postService.applyToPost(postId, user.getId(), request);
        return ResponseEntity.ok().build();
    }

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

