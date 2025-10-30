package swyp.dodream.domain.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.dto.response.master.ProfileRoleResponse;
import swyp.dodream.domain.profile.dto.response.master.ProfileInterestKeywordResponse;
import swyp.dodream.domain.profile.dto.response.master.ProfileTechStackResponse;
import swyp.dodream.domain.profile.enums.ActivityMode;
import swyp.dodream.domain.profile.enums.Experience;
import swyp.dodream.domain.url.dto.ProfileUrlResponse;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class ProfileMyPageResponse {

    private String nickname;

    private Experience experience;

    private ActivityMode activityMode;

    private String introText;

    private List<ProfileRoleResponse> roles;

    private List<ProfileInterestKeywordResponse> interestKeywords;

    private List<ProfileTechStackResponse> techSkills;

    private List<ProfileUrlResponse> profileUrls;

    public static ProfileMyPageResponse from(Profile profile) {
        return ProfileMyPageResponse.builder()
                .nickname(profile.getNickname())
                .experience(profile.getExperience())
                .activityMode(profile.getActivityMode())
                .introText(profile.getIntroText())
                .roles(profile.getRoles().stream()
                        .map(ProfileRoleResponse::from)
                        .collect(Collectors.toList()))
                .interestKeywords(profile.getInterestKeywords().stream()
                        .map(ProfileInterestKeywordResponse::from)
                        .collect(Collectors.toList()))
                .techSkills(profile.getTechSkills().stream()
                        .map(ProfileTechStackResponse::fromTechSkill)
                        .collect(Collectors.toList()))
                .profileUrls(profile.getProfileUrls().stream()
                        .map(ProfileUrlResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
