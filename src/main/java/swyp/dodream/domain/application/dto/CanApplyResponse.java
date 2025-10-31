package swyp.dodream.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모집글 지원 가능 여부 응답")
public class CanApplyResponse {

    @Schema(description = "지원 가능 여부 (true: 지원 가능, false: 지원 불가)", example = "true")
    private boolean canApply;
}
