package swyp.dodream.domain.matched.dto;

import lombok.Builder;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MatchedPostResponse(
        Long id,
        Long postId,
        String postTitle,
        String projectType,        // project / study
        String activityMode,       // online / offline / hybrid
        PostStatus postStatus,             // recruiting / completed
        String leaderName,
        String leaderProfileImage,
        ApplicationStatus myStatus, // APPLIED, WITHDRAWN, ACCEPTED, REJECTED
        LocalDateTime matchedAt,
        List<String> roles,
        List<String> stacks,
        Long viewCount,
        boolean bookmarked,
        LocalDateTime postCreatedAt
) {
    public static MatchedPostResponse from(Matched matched, boolean bookmarked) {
        Post post = matched.getPost();
        User leader = post.getOwner();

        return MatchedPostResponse.builder()
                .id(matched.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .postStatus(post.getStatus())
                .leaderName(leader.getName())
                .leaderProfileImage(leader.getProfileImageUrl())
                .myStatus(
                        // application이 있으면 그 상태, 없으면(작성자) null
                        matched.getApplication() != null
                                ? matched.getApplication().getStatus()
                                : null
                )
                .matchedAt(matched.getMatchedAt())
                .roles(
                        post.getRoleRequirements().stream()
                                .map(pr -> pr.getRole().getName())
                                .toList()
                )
                .stacks(
                        post.getStacks().stream()
                                .map(ps -> ps.getTechSkill().getName())
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