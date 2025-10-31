package swyp.dodream.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "피드백 작성 완료 응답")
@Builder
public record FeedbackCreateResponse(

        @Schema(description = "피드백 ID", example = "1")
        Long feedbackId,

        @Schema(description = "성공 메시지", example = "피드백이 성공적으로 작성되었습니다.")
        String message
) {

    /**
     * 피드백 ID → FeedbackCreateResponse
     */
    public static FeedbackCreateResponse of(Long feedbackId) {
        return FeedbackCreateResponse.builder()
                .feedbackId(feedbackId)
                .message("피드백이 성공적으로 작성되었습니다.")
                .build();
    }
}