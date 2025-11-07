package swyp.dodream.domain.recommendation.dto;

import lombok.Builder;
import swyp.dodream.domain.profile.domain.Profile;

import java.util.List;

/**
 * 추천 프로필 응답 DTO
 */
@Builder
public record RecommendationProfileResponse(
        Long profileId,
        Long userId,
        String nickname,
        String introText,
        String activityMode,
        String experience,
        Integer profileImageCode,
        Double similarity,  // 유사도 점수 (0.0 ~ 1.0)
        
        List<String> techSkills,
        List<String> interestKeywords,
        List<String> roles,
        
        List<String> tags  // 추천 태그 (#선호하는활동방식, #사용하는기술스택, #선호하는분야)
) {
    /**
     * Profile 엔티티를 RecommendationProfileResponse로 변환
     */
    public static RecommendationProfileResponse from(Profile profile, Double similarity, List<String> tags) {
        return RecommendationProfileResponse.builder()
                .profileId(profile.getId())
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .introText(profile.getIntroText())
                .activityMode(profile.getActivityMode() != null ? profile.getActivityMode().name() : null)
                .experience(profile.getExperience() != null ? profile.getExperience().name() : null)
                .profileImageCode(profile.getProfileImageCode())
                .similarity(similarity)
                .techSkills(extractTechSkills(profile))
                .interestKeywords(extractInterestKeywords(profile))
                .roles(extractRoles(profile))
                .tags(tags)
                .build();
    }

    private static List<String> extractTechSkills(Profile profile) {
        return profile.getTechSkills().stream()
                .map(tech -> tech.getName())
                .toList();
    }

    private static List<String> extractInterestKeywords(Profile profile) {
        return profile.getInterestKeywords().stream()
                .map(keyword -> keyword.getName())
                .toList();
    }

    private static List<String> extractRoles(Profile profile) {
        return profile.getRoles().stream()
                .map(role -> role.getName())
                .toList();
    }
}

