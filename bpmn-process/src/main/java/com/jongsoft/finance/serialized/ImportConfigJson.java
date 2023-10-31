package com.jongsoft.finance.serialized;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Serdeable
@ToString(of = {"accountId", "headers", "dateFormat", "delimiter"})
public class ImportConfigJson implements Serializable {

    public enum MappingRole {
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

        MappingRole(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static MappingRole value(String source) {
            for (MappingRole role : values()) {
                if (role.label.equalsIgnoreCase(source)) {
                    return role;
                }
            }
            throw new IllegalStateException("No mapping role found for " + source);
        }
    }

    @JsonProperty("has-headers")
    private boolean headers;

    @JsonProperty("apply-rules")
    private boolean applyRules;

    @JsonProperty("generate-accounts")
    private boolean generateAccounts;

    @JsonProperty("date-format")
    private String dateFormat;

    @JsonProperty("delimiter")
    private Character delimiter;

    @JsonProperty("column-roles")
    private List<MappingRole> columnRoles;

    @JsonProperty("custom-indicator")
    private CustomIndicator customIndicator;

    private Long accountId;

    @Getter
    @Setter
    @Serdeable
    public static class CustomIndicator implements Serializable {
        @JsonProperty("deposit")
        private String deposit;

        @JsonProperty("credit")
        private String credit;
    }

}
