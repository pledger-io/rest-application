package com.jongsoft.finance.serialized;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.ToString;

@Data
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
    private Sequence<MappingRole> columnRoles;

    @JsonProperty("custom-indicator")
    private CustomIndicator customIndicator;

    private Long accountId;

    @Data
    public static class CustomIndicator implements Serializable {
        @JsonProperty("deposit")
        private String deposit;

        @JsonProperty("credit")
        private String credit;
    }

    public static ImportConfigJson read(String content) {
        try {
            return ProcessMapper.INSTANCE.readValue(content, ImportConfigJson.class);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid configuration provided for import.", e);
        }
    }

    public String write() {
        try {
            return ProcessMapper.INSTANCE.writeValueAsString(this);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid configuration provided for import.");
        }
    }

}
