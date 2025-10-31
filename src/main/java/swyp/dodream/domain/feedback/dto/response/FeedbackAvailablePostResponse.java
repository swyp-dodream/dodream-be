package swyp.dodream.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import swyp.dodream.domain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "피드백 작성 가능한 게시글 정보")
@Builder
public record FeedbackAvailablePostResponse(

        @Schema(description = "모집글 ID", example = "1")
        Long postId,

        @Schema(description = "모집글 제목", example = "백엔드 개발자 모집")
        String title,

        @Schema(description = "프로젝트 타입", example = "project", allowableValues = {"project", "study"})
        String projectType,

        @Schema(description = "활동 방식", example = "online", allowableValues = {"online", "offline", "hybrid"})
        String activityMode,

        @Schema(description = "모집 상태", example = "completed", allowableValues = {"recruiting", "completed"})
        String status,

        @Schema(description = "모집 마감일", example = "2024-09-30T23:59:59")
        LocalDateTime deadlineAt,

        @Schema(description = "피드백 작성 가능 여부", example = "true")
        boolean canWriteFeedback,

        @Schema(description = "피드백 작성 가능한 멤버 목록 (본인 제외)")
        List<FeedbackMemberResponse> members
) {

    /**
     * Post + 멤버 목록 → FeedbackAvailablePostResponse
     */
    public static FeedbackAvailablePostResponse of(
            Post post,
            boolean canWriteFeedback,
            List<FeedbackMemberResponse> members
    ) {
        return FeedbackAvailablePostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .projectType(post.getProjectType().name().toLowerCase())
                .activityMode(post.getActivityMode().name().toLowerCase())
                .status(post.getStatus().name().toLowerCase())
                .deadlineAt(post.getDeadlineAt())
                .canWriteFeedback(canWriteFeedback)
                .members(members)
                .build();
    }
}