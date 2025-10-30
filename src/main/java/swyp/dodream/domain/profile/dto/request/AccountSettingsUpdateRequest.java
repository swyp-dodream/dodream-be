package swyp.dodream.domain.profile.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.enums.AgeBand;
import swyp.dodream.domain.profile.enums.Gender;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountSettingsUpdateRequest {

    @NotNull(message = "성별을 선택해야 합니다.")
    private Gender gender;

    @NotNull(message = "연령대를 선택해야 합니다.")
    private AgeBand ageBand;

    @NotNull(message = "프로젝트 제안 수신 여부를 선택해야 합니다.")
    private Boolean proposalProjectOn;

    @NotNull(message = "스터디 제안 수신 여부를 선택해야 합니다.")
    private Boolean proposalStudyOn;

    @NotNull(message = "프로필 공개 여부를 선택해야 합니다.")
    private Boolean isPublic;
}
