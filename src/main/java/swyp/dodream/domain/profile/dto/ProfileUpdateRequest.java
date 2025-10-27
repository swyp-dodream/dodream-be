package swyp.dodream.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.enums.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String nickname;
    private Gender gender;
    private AgeBand ageBand;
    private Experience experience;
    private Long roleId;
    private ActivityMode activityMode;
    private String introText;
    private Boolean introIsAi;
    private Boolean isPublic;
}
