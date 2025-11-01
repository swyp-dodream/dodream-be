package swyp.dodream.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import swyp.dodream.domain.user.domain.User;

@Schema(description = "피드백 작성 가능한 멤버 정보")
@Builder
public record FeedbackMemberResponse(

        @Schema(description = "유저 ID", example = "200")
        Long userId,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "프로필 이미지", example = "https://i.pravatar.cc/150?img=1")
        String profileImage,

        @Schema(description = "이미 피드백 작성 완료 여부", example = "false")
        boolean alreadyWritten
) {

    /**
     * User + 작성 여부 → FeedbackMemberResponse
     */
    public static FeedbackMemberResponse of(User user, boolean alreadyWritten) {
        return FeedbackMemberResponse.builder()
                .userId(user.getId())
                .nickname(user.getName())
                .profileImage(user.getProfileImageUrl())
                .alreadyWritten(alreadyWritten)
                .build();
    }
}