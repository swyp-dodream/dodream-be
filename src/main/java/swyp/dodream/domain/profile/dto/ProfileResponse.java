package swyp.dodream.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.enums.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private Gender gender;
    private AgeBand ageBand;
    private Experience experience;
    private Long roleId;
    private ActivityMode activityMode;
    private String introText;
    private Boolean introIsAi;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getNickname(),
                profile.getGender(),
                profile.getAgeBand(),
                profile.getExperience(),
                profile.getRoleId(),
                profile.getActivityMode(),
                profile.getIntroText(),
                profile.getIntroIsAi(),
                profile.getIsPublic(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
