package swyp.dodream.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import swyp.dodream.domain.interest.domain.Interest;
import swyp.dodream.domain.interest.enums.InterestEnum;

import java.time.LocalDateTime;

public record ProfileInterestResponse(
        @Schema(description = "프로필 관심분야 ID")
        Long id,

        @Schema(description = "관심 분야 이름", example = "AI")
        String interestName,

        @Schema(description = "관심 분야 카테고리", example = "기술")
        String interestCategory,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static ProfileInterestResponse of(Interest profileInterest) {
        InterestEnum interest = profileInterest.getInterest();
        return new ProfileInterestResponse(
                profileInterest.getId(),
                interest.getName(),
                interest.getCategory(),
                profileInterest.getCreatedAt()
        );
    }

    public static ProfileInterestResponse ofEnum(InterestEnum interest) {
        return new ProfileInterestResponse(
                null,
                interest.getName(),
                interest.getCategory(),
                null
        );
    }
}
