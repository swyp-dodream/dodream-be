package swyp.dodream.domain.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.dodream.domain.master.TechSkill;

@Getter
@AllArgsConstructor
public class TechSkillResponse {
    private Long id;
    private String name;
    private String categoryName;

    public static TechSkillResponse from(TechSkill skill) {
        return new TechSkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory().getName()
        );
    }
}
