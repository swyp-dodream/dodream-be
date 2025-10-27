package swyp.dodream.domain.profile.enums;

public enum ActivityMode {
    온라인("온라인"), 오프라인("오프라인"), 하이브리드("하이브리드");

    private final String value;

    ActivityMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
