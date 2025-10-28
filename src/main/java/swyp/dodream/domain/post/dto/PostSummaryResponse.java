package swyp.dodream.domain.post.dto;

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
    private Long viewCount;
    private LocalDateTime deadline;
    private String status;
    private String activityMode;

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
                .interests(post.getInterests().stream()
                        .map(i -> i.getName())
                        .toList())
                .author(post.getOwner().getName())
                .viewCount(post.getPostView().getViews())
                .deadline(post.getDeadlineAt())
                .status(post.getStatus().name())
                .activityMode(post.getActivityMode().name())
                .build();
    }
}
