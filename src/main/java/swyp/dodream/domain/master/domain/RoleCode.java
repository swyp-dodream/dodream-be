package swyp.dodream.domain.master.domain;

public enum RoleCode {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    MOBILE_IOS("iOS"),
    MOBILE_ANDROID("안드로이드"),
    DESIGNER("디자이너"),
    PLANNER("기획자"),
    MARKETER("마케터");

    private final String displayName;

    RoleCode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}