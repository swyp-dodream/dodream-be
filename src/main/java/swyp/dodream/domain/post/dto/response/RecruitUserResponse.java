package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.suggestion.domain.Suggestion;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record RecruitUserResponse(
        Long suggestionId,
        Long applicationId,
        Long userId,
        String nickname,
        String profileImage,
        String status,           // PENDING, ACCEPTED 등
        LocalDateTime createdAt, // 지원/제안/매칭 시간
        String experience,       // 프로필의 경력 (enum name)
        List<String> jobGroups   // 지원시 선택한 직군
) {

    /**
     * Suggestion → RecruitUserResponse
     * 프로필이 별도 테이블에 있으므로 같이 넘겨받도록 변경
     */
    public static RecruitUserResponse fromSuggestion(Suggestion suggestion, Profile profile) {
        User user = suggestion.getToUser();

        return RecruitUserResponse.builder()
                .suggestionId(suggestion.getId())
                .applicationId(null)
                .userId(user.getId())
                .nickname(profile != null ? profile.getNickname() : user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("PENDING")
                .createdAt(suggestion.getCreatedAt())
                .experience(profile != null && profile.getExperience() != null ? profile.getExperience().name() : null)
                .jobGroups(profile != null ? profile.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()) : List.of())
                .build();
    }

    /**
     * Application → RecruitUserResponse
     * 지원한 유저의 프로필도 같이 받아서 매핑
     */
    public static RecruitUserResponse fromApplication(Application application, Profile profile) {
        User user = application.getApplicant();

        return RecruitUserResponse.builder()
                .suggestionId(null)
                .applicationId(application.getId())
                .userId(user.getId())
                .nickname(profile != null ? profile.getNickname() : user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("PENDING")
                .createdAt(application.getCreatedAt())
                .experience(profile != null && profile.getExperience() != null ? profile.getExperience().name() : null)
                .jobGroups(application.getRole() != null ? List.of(application.getRole().getName()) : List.of())
                .build();
    }

    /**
     * Matched → RecruitUserResponse
     */
    public static RecruitUserResponse fromMatched(Matched matched, Profile profile) {
        User user = matched.getUser();

        return RecruitUserResponse.builder()
                .applicationId(matched.getApplication() != null
                        ? matched.getApplication().getId()
                        : null)
                .suggestionId(null)
                .userId(user.getId())
                .nickname(profile != null ? profile.getNickname() : user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("ACCEPTED")
                .createdAt(matched.getMatchedAt())
                .experience(profile != null && profile.getExperience() != null ? profile.getExperience().name() : null)
                .jobGroups(profile != null ? profile.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()) : List.of())
                .build();
    }
}
