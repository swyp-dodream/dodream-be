package swyp.dodream.domain.recommendation.dto;

import lombok.Builder;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 추천 게시글 응답 DTO
 */
@Builder
public record RecommendationPostResponse(
        Long postId,
        String title,
        String content,
        String projectType,
        String activityMode,
        String status,
        LocalDateTime deadlineAt,
        Long viewCount,
        Double similarity,        // 유사도 점수 (0.0 ~ 1.0)

        List<String> techStacks,
        List<String> fields,
        List<RoleRequirementDto> roles,

        String authorNickname,
        LocalDateTime createdAt
) {
    /**
     * 역할 모집 정보 DTO
     */
    @Builder
    public record RoleRequirementDto(
            String roleName,
            int headcount
    ) {}

    /**
     * Post 엔티티를 RecommendationPostResponse로 변환
     */
    public static RecommendationPostResponse from(Post post, Double similarity) {
        return RecommendationPostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .status(post.getStatus().name().toLowerCase())
                .deadlineAt(post.getDeadlineAt())
                .viewCount(post.getPostView() != null ? post.getPostView().getViews() : 0L)
                .similarity(similarity)
                .techStacks(extractTechStacks(post))
                .fields(extractFields(post))
                .roles(extractRoles(post))
                .authorNickname(post.getOwner() != null ? post.getOwner().getName() : "알 수 없음")
                .createdAt(post.getCreatedAt())
                .build();
    }

    private static List<String> extractTechStacks(Post post) {
        return post.getStacks().stream()
                .map(stack -> stack.getTechSkill().getName())
                .toList();
    }

    private static List<String> extractFields(Post post) {
        return post.getFields().stream()
                .map(field -> field.getInterestKeyword().getName())
                .toList();
    }

    private static List<RoleRequirementDto> extractRoles(Post post) {
        return post.getRoleRequirements().stream()
                .map(req -> RoleRequirementDto.builder()
                        .roleName(req.getRole().getName())
                        .headcount(req.getHeadcount())
                        .build())
                .toList();
    }
}

