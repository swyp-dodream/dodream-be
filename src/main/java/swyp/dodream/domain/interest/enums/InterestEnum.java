package swyp.dodream.domain.interest.enums;

public enum InterestEnum {
    // 기술
    AI("AI", "기술"),
    MOBILITY("모빌리티", "기술"),
    DATA("데이터", "기술"),

    // 비즈니스
    ECOMMERCE("이커머스", "비즈니스"),
    O2O("O2O", "비즈니스"),
    FINANCE("금융", "비즈니스"),

    // 사회
    ENVIRONMENT("환경", "사회"),
    LOCAL("지역", "사회"),
    EDUCATION("교육", "사회"),

    // 라이프
    FOOD_BEVERAGE("F&B", "라이프"),
    FASHION_BEAUTY("패션&뷰티", "라이프"),
    HEALTH("건강", "라이프"),
    TRAVEL("여행", "라이프"),
    SPORTS("스포츠", "라이프"),
    PET("반려동물", "라이프"),

    // 문화
    GAME("게임", "문화"),
    MEDIA("미디어", "문화"),
    ART_PERFORMANCE("예술&공연", "문화");

    private final String name;
    private final String category;

    InterestEnum(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
