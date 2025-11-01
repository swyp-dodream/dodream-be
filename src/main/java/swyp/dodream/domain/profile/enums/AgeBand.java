package swyp.dodream.domain.profile.enums;

public enum AgeBand {
    십대("10대"), 이십대("20대"), 삼십대("30대"), 
    사십대이상("40대 이상"), 선택안함("선택안함");

    private final String value;

    AgeBand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
