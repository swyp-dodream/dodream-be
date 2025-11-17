package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MyPostResponse(
        Long postId,
        String title,
        String projectType,      // "project" or "study"
        String activityMode,     // "online", "offline", "hybrid"
        String duration,         // 진행 기간
        String status,           // "recruiting" or "completed"
        LocalDateTime deadlineAt,
        Long viewCount,
        List<String> fields,     // 관심 분야 리스트
        List<RoleRequirementDto> roleRequirements,  // 모집 역할
        List<String> stacks,     // 기술 스택 리스트
        LocalDateTime createdAt,
        LocalDateTime updatedAt
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
     * Post 엔티티를 MyPostResponse로 변환
     *
     * @param post 게시글 엔티티
     * @param viewCount 조회수
     * @return MyPostResponse DTO
     */
    public static MyPostResponse from(Post post, Long viewCount) {
        return MyPostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .duration(post.getDuration().name().toLowerCase())
                .status(post.getStatus().name().toLowerCase())
                .deadlineAt(post.getDeadlineAt())
                .viewCount(viewCount)
                .fields(extractFields(post))
                .roleRequirements(extractRoleRequirements(post))
                .stacks(extractStacks(post))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 관심 분야 추출
     */
    private static List<String> extractFields(Post post) {
        return post.getFields().stream()
                .map(postField -> postField.getInterestKeyword().getName())
                .toList();
    }

    /**
     * 역할 모집 정보 추출
     */
    private static List<RoleRequirementDto> extractRoleRequirements(Post post) {
        return post.getRoleRequirements().stream()
                .map(postRole -> RoleRequirementDto.builder()
                        .roleName(postRole.getRole().getName())
                        .headcount(postRole.getHeadcount())
                        .build())
                .toList();
    }

    /**
     * 기술 스택 추출
     */
    private static List<String> extractStacks(Post post) {
        return post.getStacks().stream()
                .map(postStack -> postStack.getTechSkill().getName())
                .toList();
    }
}