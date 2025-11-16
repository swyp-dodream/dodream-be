package swyp.dodream.domain.master.domain;

public enum ApplicationStatus implements MatchingStatusType {
    APPLIED,    // 지원 완료
    WITHDRAWN,  // 지원 취소
    ACCEPTED,   // 수락됨
    REJECTED;   // 거절됨

    @Override
    public boolean isActive() {
        return this == APPLIED || this == ACCEPTED || this == REJECTED;
    }
}