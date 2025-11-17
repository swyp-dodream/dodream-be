package swyp.dodream.domain.suggestion.dto;

import lombok.Builder;
import swyp.dodream.domain.master.domain.SuggestionStatus;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.suggestion.domain.Suggestion;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SuggestionResponse(
        // 기존 제안 정보
        Long id,
        Long postId,
        Long toUserId,
        Long fromUserId,
        String suggestionMessage,
        LocalDateTime suggestedAt,

        // Post 상세 정보 추가
        String postTitle,
        String projectType,        // project / study
        String activityMode,       // online / offline / hybrid
        PostStatus postStatus,             // recruiting / completed
        String leaderName,
        String leaderProfileImage,
        SuggestionStatus suggestionStatus,  // SENT, CANCELED, ACCEPTED, REJECTED
        List<String> roles,
        List<String> stacks,
        Long viewCount,
        LocalDateTime postCreatedAt,

        boolean bookmarked
) {
    public static SuggestionResponse from(Suggestion suggestion, boolean bookmarked) {
        Post post = suggestion.getPost();
        User leader = post.getOwner();

        return SuggestionResponse.builder()
                // 제안 정보
                .id(suggestion.getId())
                .postId(post.getId())
                .toUserId(suggestion.getToUser().getId())
                .fromUserId(suggestion.getFromUser().getId())
                .suggestionMessage(suggestion.getSuggestionMessage())
                .suggestedAt(suggestion.getCreatedAt())

                // Post 상세 정보
                .postTitle(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .postStatus(post.getStatus())
                .leaderName(leader.getName())
                .leaderProfileImage(leader.getProfileImageUrl())
                .suggestionStatus(suggestion.getStatus())
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

                .bookmarked(bookmarked)
                .build();
    }
}