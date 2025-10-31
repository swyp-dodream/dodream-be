package swyp.dodream.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import swyp.dodream.domain.feedback.domain.FeedbackOption;

@Schema(description = "피드백 옵션 정보")
@Builder
public record FeedbackOptionResponse(

        @Schema(description = "옵션 코드", example = "GOOD_COMMUNICATION")
        String code,

        @Schema(description = "옵션 설명", example = "소통이 원활해요")
        String description
) {

    /**
     * FeedbackOption → FeedbackOptionResponse
     */
    public static FeedbackOptionResponse from(FeedbackOption option) {
        return FeedbackOptionResponse.builder()
                .code(option.name())
                .description(option.getDescription())
                .build();
    }
}