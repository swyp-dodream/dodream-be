package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
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
    String status,             // recruiting / completed
    String leaderName,
    String leaderProfileImage,
    String myStatus,           // PENDING, ACCEPTED, REJECTED
    LocalDateTime appliedAt,
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
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("PENDING")  // 지원 상태
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

    /**
     * Suggestion → MyApplicationResponse
     */
    public static MyApplicationResponse fromSuggestion(Suggestion suggestion, boolean bookmarked) {
        Post post = suggestion.getPost();
        User leader = post.getOwner();

        return MyApplicationResponse.builder()
            .id(suggestion.getId())
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("SUGGESTED")  // 제안받음
            .appliedAt(suggestion.getCreatedAt())
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
            .postCreatedAt(post.getCreatedAt())
            .build();
    }

    /**
     * Matched → MyApplicationResponse
     */
    public static MyApplicationResponse fromMatched(Matched matched, boolean bookmarked) {
        Post post = matched.getPost();
        User leader = post.getOwner();

        return MyApplicationResponse.builder()
            .id(matched.getId())
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("ACCEPTED")  // 수락됨
            .appliedAt(matched.getMatchedAt())
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
            .postCreatedAt(post.getCreatedAt())
            .build();
    }
}