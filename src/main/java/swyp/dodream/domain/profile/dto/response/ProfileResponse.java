package swyp.dodream.domain.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.dto.response.master.ProfileRoleResponse;
import swyp.dodream.domain.profile.dto.response.master.ProfileInterestKeywordResponse;
import swyp.dodream.domain.profile.dto.response.master.ProfileTechStackResponse;
import swyp.dodream.domain.profile.enums.*;
import swyp.dodream.domain.proposal.domain.ProposalNotification;
import swyp.dodream.domain.url.dto.ProfileUrlResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String gender;
    private String ageBand;
    private String experience;
    private String activityMode;
    private String introText;
    private boolean isPublic;

    private List<String> roles;
    private List<String> interestKeywords;
    private List<String> techSkills;

    private boolean proposalProjectOn;
    private boolean proposalStudyOn;

    public static ProfileResponse from(Profile p, ProposalNotification pn) {
        return ProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .nickname(p.getNickname())
                .gender(p.getGender() == null ? null : p.getGender().name())
                .ageBand(p.getAgeBand() == null ? null : p.getAgeBand().name())
                .experience(p.getExperience() == null ? null : p.getExperience().name())
                .activityMode(p.getActivityMode() == null ? null : p.getActivityMode().name())
                .introText(p.getIntroText())
                .isPublic(Boolean.TRUE.equals(p.getIsPublic()))
                .roles(p.getRoles().stream().map(r -> r.getCode().name()).collect(Collectors.toList()))
                .interestKeywords(p.getInterestKeywords().stream().map(k -> k.getName()).collect(Collectors.toList()))
                .techSkills(p.getTechSkills().stream().map(s -> s.getName()).collect(Collectors.toList()))
                .proposalProjectOn(pn.getProposalProjectOn())
                .proposalStudyOn(pn.getProposalStudyOn())
                .build();
    }
}
