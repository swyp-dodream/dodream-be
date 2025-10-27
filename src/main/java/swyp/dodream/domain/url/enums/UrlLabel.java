package swyp.dodream.domain.url.enums;

public enum UrlLabel {
    깃허브("GitHub"), 노션("Notion"), 포트폴리오("Portfolio"), 
    블로그("Blog"), 기타("Etc");

    private final String displayName;

    UrlLabel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
