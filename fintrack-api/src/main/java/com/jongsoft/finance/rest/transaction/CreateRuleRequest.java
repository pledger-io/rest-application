package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Builder
@NoArgsConstructor
@Serdeable.Deserializable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CreateRuleRequest {

    @Introspected
    @NoArgsConstructor
    public static class Condition {
        @Schema(description = "The identifier of an existing pre-condition")
        private Long id;

        @NotNull
        @Schema(description = "The column on which to look for the pre-condition")
        private RuleColumn column;

        @NotNull
        @Schema(description = "The type of comparison operation to perform")
        private RuleOperation operation;

        @NotBlank
        @Schema(description = "The value the column must have to match the pre-condition", example = "My personal account")
        private String value;

        public Condition(Long id, RuleColumn column, RuleOperation operation, String value) {
            this.id = id;
            this.column = column;
            this.operation = operation;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public RuleColumn getColumn() {
            return column;
        }

        public RuleOperation getOperation() {
            return operation;
        }

        public String getValue() {
            return value;
        }
    }

    @Introspected
    @NoArgsConstructor
    public static class Change {

        @Schema(description = "The identifier of an already existing change")
        private Long id;
        @NotNull
        @Schema(description = "The column on which the change is effected", example = "CATEGORY")
        private RuleColumn column;
        @NotBlank
        @Schema(description = "The value to be applied, this could be an identifier", example = "1")
        private String value;

        public Change(Long id, @NotNull RuleColumn column, @NotBlank String value) {
            this.id = id;
            this.column = column;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public RuleColumn getColumn() {
            return column;
        }

        public String getValue() {
            return value;
        }
    }

    @NotBlank
    @Size(max = 255)
    @Schema(description = "The name of the rule", implementation = String.class, required = true)
    private String name;

    @Size(max = 1024)
    @Schema(description = "A long description of the rule", implementation = String.class)
    private String description;

    @Schema(description = "Should the rule execution stop after a positive match")
    private boolean restrictive;

    @Schema(description = "Should the rule be executed when the engine runs")
    private boolean active;

    @NotNull
    @Size(min = 1)
    @Schema(description = "List of all pre-conditions that must be met")
    private List<Condition> conditions;

    @NotNull
    @Size(min = 1)
    @Schema(description = "List of all the changes to be applied")
    private List<Change> changes;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRestrictive() {
        return restrictive;
    }

    public boolean isActive() {
        return active;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Change> getChanges() {
        return changes;
    }
}
