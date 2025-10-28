package swyp.dodream.domain.techstack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import swyp.dodream.domain.techstack.enums.TechStackEnum;

public record ProfileTechStackRequest(
        @Schema(description = "기술 스택", example = "Spring")
        @NotNull
        TechStackEnum techStack
) {
}
