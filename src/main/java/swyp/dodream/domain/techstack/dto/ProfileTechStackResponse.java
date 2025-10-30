package swyp.dodream.domain.techstack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import swyp.dodream.domain.techstack.domain.TechStack;
import swyp.dodream.domain.techstack.enums.TechStackEnum;

import java.time.LocalDateTime;

public record ProfileTechStackResponse(
        @Schema(description = "프로필 기술스택 ID")
        Long id,

        @Schema(description = "기술 스택 상세 이름", example = "Spring")
        String techStackName,

        @Schema(description = "기술 스택 카테고리", example = "백엔드")
        String techStackCategory,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static ProfileTechStackResponse of(TechStack profileTechStack) {
        TechStackEnum techStack = profileTechStack.getTechStack();
        return new ProfileTechStackResponse(
                profileTechStack.getId(),
                techStack.getName(),
                techStack.getCategory(),
                profileTechStack.getCreatedAt()
        );
    }

    public static ProfileTechStackResponse ofEnum(TechStackEnum techStack) {
        return new ProfileTechStackResponse(
                null,
                techStack.getName(),
                techStack.getCategory(),
                null
        );
    }
}
