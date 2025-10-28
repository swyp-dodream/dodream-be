package swyp.dodream.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import swyp.dodream.domain.interest.enums.InterestEnum;

public record ProfileInterestRequest(
        @Schema(description = "관심 분야", example = "AI")
        @NotNull
        InterestEnum interest
) {
}
