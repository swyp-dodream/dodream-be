package swyp.dodream.domain.techstack.enums;

public enum TechStackEnum {
    // 프론트엔드
    JAVASCRIPT("JavaScript", "프론트엔드"),
    TYPESCRIPT("TypeScript", "프론트엔드"),
    REACT("React", "프론트엔드"),
    VUE("Vue", "프론트엔드"),
    SVELTE("Svelte", "프론트엔드"),
    NEXTJS("Nextjs", "프론트엔드"),

    // 백엔드
    JAVA("Java", "백엔드"),
    SPRING("Spring", "백엔드"),
    NODEJS("Nodejs", "백엔드"),
    NESTJS("Nestjs", "백엔드"),
    GO("Go", "백엔드"),
    KOTLIN("Kotlin", "백엔드"),
    EXPRESS("Express", "백엔드"),
    MYSQL("MySQL", "백엔드"),
    MONGODB("MongoDB", "백엔드"),
    RUBY("Ruby", "백엔드"),
    PYTHON("Python", "백엔드"),
    DJANGO("Django", "백엔드"),
    PHP("PHP", "백엔드"),
    GRAPHQL("GraphQL", "백엔드"),
    FIREBASE("Firebase", "백엔드"),

    // 모바일
    SWIFT("Swift", "모바일"),
    OBJECTIVE_C("Objective-C", "모바일"),
    KOTLIN_MOBILE("Kotlin", "모바일"),
    JAVA_MOBILE("Java", "모바일"),
    FLUTTER("Flutter", "모바일"),
    REACT_NATIVE("ReactNative", "모바일"),

    // 디자인
    ZEPLIN("Zeplin", "디자인"),
    FIGMA("Figma", "디자인"),
    SKETCH("Sketch", "디자인"),
    ADOBE_XD("AdobeXD", "디자인");

    private final String name;
    private final String category;

    TechStackEnum(String name, String category) {
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
