package swyp.dodream.domain.feedback.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record FeedbackMemberListResponse(
        List<FeedbackMemberResponse> members
) {
    public static FeedbackMemberListResponse of(List<FeedbackMemberResponse> members) {
        return new FeedbackMemberListResponse(members);
    }
}