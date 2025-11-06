package swyp.dodream.domain.post.dto.response;

import lombok.Builder;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Builder
public record RecruitUserResponse(
        Long suggestionId,
        Long applicationId,
        Long userId,
        String nickname,
        String profileImage,
        String status,           // PENDING, ACCEPTED 등
        LocalDateTime createdAt  // 지원/제안/매칭 시간
) {

    /**
     * Suggestion → RecruitUserResponse
     */
    public static RecruitUserResponse fromSuggestion(Suggestion suggestion) {
        User user = suggestion.getToUser();

        return RecruitUserResponse.builder()
                .suggestionId(suggestion.getId())
                .applicationId(null)
                .userId(user.getId())
                .nickname(user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("PENDING")
                .createdAt(suggestion.getCreatedAt())
                .build();
    }

    /**
     * Application → RecruitUserResponse
     */
    public static RecruitUserResponse fromApplication(Application application) {
        User user = application.getApplicant();

        return RecruitUserResponse.builder()
                .suggestionId(null)    // 지원 이니까 제안 id에 데해서 null 처리
                .applicationId(application.getId())
                .userId(user.getId())
                .nickname(user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("PENDING")
                .createdAt(application.getCreatedAt())
                .build();
    }

    /**
     * Matched → RecruitUserResponse
     */
    public static RecruitUserResponse fromMatched(Matched matched) {
        User user = matched.getUser();

        return RecruitUserResponse.builder()
                .suggestionId(null)     // 메칭 이니까 제안 id에 대해서 null 처리
                .applicationId(null)
                .userId(user.getId())
                .nickname(user.getName())
                .profileImage(user.getProfileImageUrl())
                .status("ACCEPTED")
                .createdAt(matched.getMatchedAt())
                .build();
    }
}