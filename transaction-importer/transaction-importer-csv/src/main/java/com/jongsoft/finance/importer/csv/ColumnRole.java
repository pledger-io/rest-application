package com.jongsoft.finance.importer.csv;

public enum ColumnRole {
    IGNORE("_ignore"),
    DATE("transaction-date"),
    BOOK_DATE("booking-date"),
    INTEREST_DATE("interest-date"),
    OPPOSING_NAME("opposing-name"),
    OPPOSING_IBAN("opposing-iban"),
    ACCOUNT_IBAN("account-iban"),
    AMOUNT("amount"),
    CUSTOM_INDICATOR("custom-indicator"),
    DESCRIPTION("description");

    private final String label;

    ColumnRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ColumnRole value(String source) {
        for (ColumnRole role : values()) {
            if (role.label.equalsIgnoreCase(source)) {
                return role;
            }
        }
        throw new IllegalStateException("No mapping role found for " + source);
    }
}
