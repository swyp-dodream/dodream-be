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
}

