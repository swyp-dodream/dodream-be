package swyp.dodream.domain.master.domain;

public enum SuggestionStatus implements MatchingStatusType {
    SENT,       // 리더가 제안 보냄
    CANCELED,   // 리더가 제안 취소
    ACCEPTED,   // 유저가 수락
    REJECTED;   // 유저가 거절

    @Override
    public boolean isActive() {
        return this == SENT;
    }
}