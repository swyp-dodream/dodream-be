package swyp.dodream.domain.application.dto.response;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MyApplicationResponse(
        Long id,
        Long postId,
        String postTitle,
        String projectType,        // project / study
        String activityMode,       // online / offline / hybrid
        PostStatus postStatus,             // recruiting / completed
        String leaderName,
        String leaderProfileImage,
        ApplicationStatus myStatus, // APPLIED, WITHDRAWN, ACCEPTED, REJECTED
        LocalDateTime appliedAt,    // 지원 시간
        List<String> roles,
        List<String> stacks,
        Long viewCount,
        boolean bookmarked, // 북마크 여부
        LocalDateTime postCreatedAt
) {

    /**
     * Application → MyApplicationResponse
     */
    public static MyApplicationResponse fromApplication(Application application, boolean bookmarked) {
        Post post = application.getPost();
        User leader = post.getOwner();

        return MyApplicationResponse.builder()
            .id(application.getId())
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .postStatus(post.getStatus())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus(application.getStatus())  // 지원 상태
            .appliedAt(application.getCreatedAt())
            .roles(
                    post.getRoleRequirements().stream()
                            .map(pr -> pr.getRole().getName())       // PostRole → Role → name
                            .toList()
            )
            .stacks(
                    post.getStacks().stream()
                            .map(ps -> ps.getTechSkill().getName())  // PostStack → TechSkill → name
                            .toList()
            )
            .viewCount(
                    post.getPostView() != null ? post.getPostView().getViews() : 0L
            )
            .bookmarked(bookmarked)
            .postCreatedAt(post.getCreatedAt())
            .build();
    }
}