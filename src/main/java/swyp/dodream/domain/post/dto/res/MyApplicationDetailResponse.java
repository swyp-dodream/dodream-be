package swyp.dodream.domain.post.dto.res;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;

import java.time.LocalDateTime;

@Builder
public record MyApplicationDetailResponse(
        Long applicationId,
        Long postId,
        String postTitle,
        String projectType,        // project / study
        String activityMode,       // online / offline / hybrid
        String status,             // recruiting / completed
        String leaderName,
        String leaderProfileImage,
        String roleName,           // 지원한 직군 이름
        String roleCode,           // 지원한 직군 코드
        String message,            // 지원 메시지
        LocalDateTime appliedAt
) {

    /**
     * Application → MyApplicationDetailResponse
     */
    public static MyApplicationDetailResponse fromApplication(Application application) {
        var post = application.getPost();
        var leader = post.getOwner();
        var role = application.getRole();

        return MyApplicationDetailResponse.builder()
                .applicationId(application.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .status(post.getStatus().name().toLowerCase())
                .leaderName(leader.getName())
                .leaderProfileImage(leader.getProfileImageUrl())
                .roleName(role.getName())
                .roleCode(String.valueOf(role.getCode()))
                .message(application.getMessage())
                .appliedAt(application.getCreatedAt())
                .build();
    }
}