package swyp.dodream.domain.post.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "활동 기간 선택지")
public enum DurationPeriod {

    @Schema(description = "미정")
    UNDECIDED("미정"),

    @Schema(description = "1개월 미만")
    UNDER_ONE_MONTH("1개월 미만"),

    @Schema(description = "1개월")
    ONE_MONTH("1개월"),

    @Schema(description = "2개월")
    TWO_MONTHS("2개월"),

    @Schema(description = "3개월")
    THREE_MONTHS("3개월"),

    @Schema(description = "4개월")
    FOUR_MONTHS("4개월"),

    @Schema(description = "5개월")
    FIVE_MONTHS("5개월"),

    @Schema(description = "6개월")
    SIX_MONTHS("6개월"),

    @Schema(description = "장기")
    LONG_TERM("장기");

    private final String label;

    DurationPeriod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
