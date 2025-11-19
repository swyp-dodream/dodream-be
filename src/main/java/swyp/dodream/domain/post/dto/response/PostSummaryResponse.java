package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostSummaryResponse {
    private Long id;
    private String title;
    private String projectType;
    private List<String> roles;
    private List<String> techs;
    private List<String> interests;
    private String author;
    private Integer authorProfileImageCode;
    private Long viewCount;
    private LocalDateTime deadline;
    private String status;
    private String activityMode;
    private LocalDateTime createdAt;

    public static PostSummaryResponse fromEntity(Post post) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .projectType(post.getProjectType().name())
                .roles(post.getRoleRequirements().stream()
                        .map(r -> r.getRole().getName())
                        .toList())
                .techs(post.getStacks().stream()
                        .map(s -> s.getTechSkill().getName())
                        .toList())
                .interests(post.getFields().stream()
                        .map(f -> f.getInterestKeyword().getName())
                        .toList())
                .author(post.getOwner().getName())
                .viewCount(post.getPostView() != null ? post.getPostView().getViews() : 0L)
                .deadline(post.getDeadlineAt())
                .status(post.getStatus().name())
                .activityMode(post.getActivityMode().name())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static PostSummaryResponse fromEntity(Post post, Integer authorProfileImageCode) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .projectType(post.getProjectType().name())
                .roles(post.getRoleRequirements().stream()
                        .map(r -> r.getRole().getName())
                        .toList())
                .techs(post.getStacks().stream()
                        .map(s -> s.getTechSkill().getName())
                        .toList())
                .interests(post.getFields().stream()
                        .map(f -> f.getInterestKeyword().getName())
                        .toList())
                .author(post.getOwner().getName())
                .authorProfileImageCode(authorProfileImageCode)
                .viewCount(post.getPostView() != null ? post.getPostView().getViews() : 0L)
                .deadline(post.getDeadlineAt())
                .status(post.getStatus().name())
                .activityMode(post.getActivityMode().name())
                .createdAt(post.getCreatedAt())

                .build();
    }
}