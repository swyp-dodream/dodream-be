package swyp.dodream.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import swyp.dodream.domain.feedback.domain.Feedback;
import swyp.dodream.domain.feedback.domain.FeedbackOption;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "받은 피드백 정보 (익명)")
@Builder
public record FeedbackReceivedResponse(

        @Schema(description = "피드백 ID", example = "1")
        Long feedbackId,

        @Schema(description = "모집글 ID", example = "1")
        Long postId,

        @Schema(description = "모집글 제목", example = "백엔드 개발자 모집")
        String postTitle,

        @Schema(description = "피드백 타입", example = "POSITIVE", allowableValues = {"POSITIVE", "NEGATIVE"})
        String feedbackType,

        @Schema(description = "상세 피드백 옵션", example = "[\"GOOD_COMMUNICATION\", \"KEEPS_PROMISES\"]")
        List<String> options,

        @Schema(description = "피드백 받은 시간", example = "2025-10-31T12:30:00")
        LocalDateTime receivedAt
) {

    /**
     * Feedback → FeedbackReceivedResponse (익명)
     */
    public static FeedbackReceivedResponse from(Feedback feedback) {
        return FeedbackReceivedResponse.builder()
                .feedbackId(feedback.getId())
                .postId(feedback.getPost().getId())
                .postTitle(feedback.getPost().getTitle())
                .feedbackType(feedback.getFeedbackType().name())
                .options(feedback.getOptions().stream()
                        .map(FeedbackOption::getDescription)
                        .collect(Collectors.toList()))
                .receivedAt(feedback.getCreatedAt())
                .build();
    }
}