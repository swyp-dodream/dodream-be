package swyp.dodream.domain.post.dto.res;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Builder
public record MyApplicationResponse(
    Long postId,
    String postTitle,
    String projectType,        // project / study
    String activityMode,       // online / offline / hybrid
    String status,             // recruiting / completed
    String leaderName,
    String leaderProfileImage,
    String myStatus,           // PENDING, ACCEPTED, REJECTED
    LocalDateTime appliedAt
) {
    
    /**
     * Application → MyApplicationResponse
     */
    public static MyApplicationResponse fromApplication(Application application) {
        Post post = application.getPost();
        User leader = post.getOwner();
        
        return MyApplicationResponse.builder()
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("PENDING")  // 지원 상태
            .appliedAt(application.getCreatedAt())
            .build();
    }
    
    /**
     * Suggestion → MyApplicationResponse
     */
    public static MyApplicationResponse fromSuggestion(Suggestion suggestion) {
        Post post = suggestion.getPost();
        User leader = post.getOwner();
        
        return MyApplicationResponse.builder()
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("SUGGESTED")  // 제안받음
            .appliedAt(suggestion.getCreatedAt())
            .build();
    }
    
    /**
     * Matched → MyApplicationResponse
     */
    public static MyApplicationResponse fromMatched(Matched matched) {
        Post post = matched.getPost();
        User leader = post.getOwner();
        
        return MyApplicationResponse.builder()
            .postId(post.getId())
            .postTitle(post.getTitle())
            .projectType(post.getProjectType().name().toLowerCase())
            .activityMode(post.getActivityMode().name().toLowerCase())
            .status(post.getStatus().name().toLowerCase())
            .leaderName(leader.getName())
            .leaderProfileImage(leader.getProfileImageUrl())
            .myStatus("ACCEPTED")  // 수락됨
            .appliedAt(matched.getMatchedAt())
            .build();
    }
}