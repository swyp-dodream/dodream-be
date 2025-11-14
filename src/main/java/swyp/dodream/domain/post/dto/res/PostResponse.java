package swyp.dodream.domain.post.dto.res;

import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponse {

    // 기본 정보
    private Long id;
    private String title;
    private String content;
    private PostStatus status;
    private boolean isOwner;
    private LocalDateTime createdAt;
    private String ownerNickname;
    private String ownerProfileImageUrl;
    private String projectType;
    private String activityMode;
    private String duration;
    private LocalDate deadlineDate;
    private List<String> interestKeywords;
    private long viewCount;
    private List<String> stacks;

    private List<RoleRequirementRes> roles;

    @Getter
    @Builder
    public static class RoleRequirementRes {
        private String role;      // ex) "백엔드"
        private int headcount;    // ex) 2
    }

    public static PostResponse from(
            Post post,
            boolean isOwner,
            String ownerNickname,
            String ownerProfileImageUrl
    ) {

        List<String> interestNames = post.getFields().stream()
                .map(pf -> pf.getInterestKeyword().getName())
                .collect(Collectors.toList());

        List<String> stackNames = post.getStacks().stream()
                .map(ps -> ps.getTechSkill().getName())
                .collect(Collectors.toList());

        List<RoleRequirementRes> roles = post.getRoleRequirements().stream()
                .map(pr -> RoleRequirementRes.builder()
                        .role(pr.getRole().getName())
                        .headcount(pr.getHeadcount())
                        .build()
                )
                .collect(Collectors.toList());

        long viewCount = (post.getPostView() != null)
                ? post.getPostView().getViews()
                : 0L;

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .isOwner(isOwner)
                .createdAt(post.getCreatedAt())
                .ownerNickname(ownerNickname)
                .ownerProfileImageUrl(ownerProfileImageUrl)

                // 모집 요약 영역
                .projectType(post.getProjectType().name())
                .activityMode(post.getActivityMode().name())
                .duration(post.getDuration().name())
                .deadlineDate(post.getDeadlineAt() != null
                        ? post.getDeadlineAt().toLocalDate()
                        : null)

                .interestKeywords(interestNames)
                .stacks(stackNames)
                .roles(roles)
                .viewCount(viewCount)
                .build();
    }
}
