package swyp.dodream.domain.profile.dto.response.master;

import swyp.dodream.domain.master.domain.InterestKeyword;

public record ProfileInterestKeywordResponse(
        Long id,
        Long categoryId,
        String name
) {
    public static ProfileInterestKeywordResponse from(InterestKeyword interestKeyword) {
        return new ProfileInterestKeywordResponse(
                interestKeyword.getId(),
                interestKeyword.getCategory().getId(),
                interestKeyword.getName()
        );
    }
}
