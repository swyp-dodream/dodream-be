package swyp.dodream.domain.role.enums;

public enum RoleEnum {
    FE("FE", "프론트엔드"),
    BE("BE", "백엔드"),
    iOS("iOS", "iOS 개발자"),
    AOS("AOS", "안드로이드 개발자"),
    DESIGNER("Designer", "디자이너"),
    PM("PM", "프로덕트 매니저"),
    PLANNER("Planner", "기획자"),
    MARKETER("Marketer", "마케터");

    private final String code;
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
