package swyp.dodream.domain.bookmark.dto.response;

import lombok.Builder;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MyBookmarkResponse(
        Long postId,
        String postTitle,
        String projectType,  // project / study
        String activityMode, // online / offline / hybrid
        PostStatus postStatus, // RECRUITING / COMPLETED
        String leaderName,
        Integer leaderProfileImageCode,
        List<String> roles,
        List<String> stacks,
        Long viewCount,
        boolean bookmarked, // 북마크 여부 - 북마크 목록 조회라 무조건 ture
        LocalDateTime postCreatedAt,
        LocalDateTime bookmarkCreatedAt
        ) {
    public static MyBookmarkResponse from(Bookmark bookmark, Profile leaderProfile) {
        Post post = bookmark.getPost();
        User leader = post.getOwner();

        return MyBookmarkResponse.builder()
                .postId(post.getId())
                .postTitle(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .postStatus(post.getStatus())
                .leaderName(leaderProfile != null ? leaderProfile.getNickname() : leader.getName())
                .leaderProfileImageCode(
                        leaderProfile != null
                                ? leaderProfile.getProfileImageCode()
                                : null  // 혹은 기본값
                )
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
                .viewCount(post.getPostView() != null ? post.getPostView().getViews() : 0L)
                .bookmarked(true)
                .postCreatedAt(post.getCreatedAt())
                .bookmarkCreatedAt(bookmark.getCreatedAt())
                .build();
    }
}