package swyp.dodream.domain.profile.dto.response.master;

import swyp.dodream.domain.master.domain.TechSkill;

public record ProfileTechStackResponse(
        Long id,
        Long categoryId,
        String name
) {
    public static ProfileTechStackResponse fromTechSkill(TechSkill techSkill) {
        return new ProfileTechStackResponse(
                techSkill.getId(),
                techSkill.getCategory().getId(),
                techSkill.getName()
        );
    }
}
