package swyp.dodream.domain.profile.enums;

public enum Experience {
    신입("신입"), 일년이상삼년미만("1년 이상 3년 미만"), 삼년이상오년미만("3년 이상 5년 미만"), 오년이상("5년 이상");

    private final String value;

    Experience(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
