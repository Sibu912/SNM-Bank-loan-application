package io.github.sibu912.snmbank.loans.domain;

public enum Title {
    MR("Mr"),
    MRS("Mrs"),
    MISS("Miss"),
    MS("Ms");

    private final String displayName;

    Title(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
