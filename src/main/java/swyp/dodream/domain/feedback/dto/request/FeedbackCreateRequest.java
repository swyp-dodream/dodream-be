package swyp.dodream.domain.feedback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import swyp.dodream.domain.feedback.domain.FeedbackOption;
import swyp.dodream.domain.feedback.domain.FeedbackType;

import java.util.List;

@Schema(description = "피드백 작성 요청")
public record FeedbackCreateRequest(

        @Schema(description = "모집글 ID", example = "1", required = true)
        @NotNull(message = "모집글 ID는 필수입니다.")
        Long postId,

        @Schema(description = "피드백 받을 유저 ID", example = "200", required = true)
        @NotNull(message = "피드백 받을 유저 ID는 필수입니다.")
        Long toUserId,

        @Schema(
                description = "피드백 타입 (POSITIVE: 좋았어요, NEGATIVE: 아쉬웠어요)",
                example = "POSITIVE",
                required = true,
                allowableValues = {"POSITIVE", "NEGATIVE"}
        )
        @NotNull(message = "피드백 타입은 필수입니다.")
        FeedbackType feedbackType,

        @Schema(
                description = "상세 피드백 옵션 (최대 3개, 긍정/부정 자유 선택)",
                example = "[\"GOOD_COMMUNICATION\", \"KEEPS_PROMISES\", \"POOR_COMMUNICATION\"]",
                allowableValues = {
                        "GOOD_COMMUNICATION", "KEEPS_PROMISES", "RESPONSIBLE",
                        "POSITIVE_ENERGY", "PROBLEM_SOLVER", "RESPECTS_OPINIONS",
                        "POOR_COMMUNICATION", "IGNORES_OPINIONS", "LACKS_RESPONSIBILITY",
                        "NEGATIVE_INFLUENCE", "POOR_PROBLEM_SOLVING", "BREAKS_PROMISES"
                }
        )
        @Size(max = 3, message = "피드백 옵션은 최대 3개까지 선택할 수 있습니다.")
        List<FeedbackOption> options
) {
}