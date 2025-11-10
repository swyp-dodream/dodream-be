package swyp.dodream.domain.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    PROPOSAL_SENT("리더가 보낸 제안"),
    PROPOSAL_APPLIED("제안받은 유저의 지원"),
    APPLICATION_ACCEPTED("지원 수락"),
    BOOKMARK_DEADLINE("북마크한 모집글 마감 임박"),
    REVIEW_ACTIVATED("후기 작성 활성화"),
    FEEDBACK_WRITTEN("프로필 후기가 등록됨");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
