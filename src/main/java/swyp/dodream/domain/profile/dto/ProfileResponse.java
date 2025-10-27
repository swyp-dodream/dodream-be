package swyp.dodream.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.enums.*;
import swyp.dodream.domain.url.dto.ProfileUrlResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private ActivityMode activityMode;
    private String introText;
    private Boolean introIsAi;
    private Boolean isPublic;
    private List<ProfileUrlResponse> profileUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(Profile profile) {
        List<ProfileUrlResponse> profileUrls = profile.getProfileUrls().stream()
                .map(ProfileUrlResponse::from)
                .collect(Collectors.toList());
        
        return new ProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getNickname(),
                profile.getGender(),
                profile.getAgeBand(),
                profile.getExperience(),
                profile.getActivityMode(),
                profile.getIntroText(),
                profile.getIntroIsAi(),
                profile.getIsPublic(),
                profileUrls,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
