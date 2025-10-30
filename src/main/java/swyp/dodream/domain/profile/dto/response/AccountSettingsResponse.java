package swyp.dodream.domain.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.profile.enums.AgeBand;
import swyp.dodream.domain.profile.enums.Gender;

@Getter
@AllArgsConstructor
@Builder
public class AccountSettingsResponse {

    private Gender gender;

    private AgeBand ageBand;

    private Boolean proposalProjectOn;

    private Boolean proposalStudyOn;

    private Boolean isPublic;

}
