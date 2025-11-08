package swyp.dodream.domain.post.dto.res;

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

    // createdAt 필드를 DTO에 추가합니다.
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

                // post 객체가 BaseEntity로부터 물려받은 getCreatedAt()을 호출해 DTO에 매핑합니다.
                .createdAt(post.getCreatedAt())

                .build();
    }
}