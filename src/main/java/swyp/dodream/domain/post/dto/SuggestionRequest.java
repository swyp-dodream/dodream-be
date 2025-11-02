package swyp.dodream.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 제안 요청 DTO")
public record SuggestionRequest(
        @Schema(description = "제안 받는 회원 ID", example = "110535236839804928")
        @NotNull(message = "제안 받는 회원 ID는 필수입니다.")
        Long toUserId,

        @Schema(description = "제안 메시지", example = "함께 팀 프로젝트를 해보고 싶어요!")
        @Size(max = 1000, message = "제안 메시지는 1000자 이하여야 합니다.")
        String suggestionMessage
) {}