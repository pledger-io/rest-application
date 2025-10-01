package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Serdeable
class CreateRuleRequest {

    @Serdeable
    public record Condition(
            @Schema(description = "The identifier of an existing pre-condition") Long id,
            @NotNull @Schema(description = "The column on which to look for the pre-condition")
                    RuleColumn column,
            @NotNull @Schema(description = "The type of comparison operation to perform")
                    RuleOperation operation,
            @NotBlank
                    @Schema(
                            description =
                                    "The value the column must have to match the pre-condition",
                            example = "My personal account")
                    String value) {}

    @Serdeable
    public record Change(
            @Schema(description = "The identifier of an already existing change") Long id,
            @NotNull
                    @Schema(
                            description = "The column on which the change is effected",
                            example = "CATEGORY")
                    RuleColumn column,
            @NotBlank
                    @Schema(
                            description = "The value to be applied, this could be an identifier",
                            example = "1")
                    String value) {}

    @NotBlank
    @Size(max = 255)
    @Schema(description = "The name of the rule", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String name;

    @Size(max = 1024)
    @Schema(description = "A long description of the rule")
    private final String description;

    @Schema(description = "Should the rule execution stop after a positive match")
    private final boolean restrictive;

    @Schema(description = "Should the rule be executed when the engine runs")
    private final boolean active;

    @NotNull
    @Size(min = 1)
    @Schema(description = "List of all pre-conditions that must be met")
    private final List<Condition> conditions;

    @NotNull
    @Size(min = 1)
    @Schema(description = "List of all the changes to be applied")
    private final List<Change> changes;

    public CreateRuleRequest(
            String name,
            String description,
            boolean restrictive,
            boolean active,
            List<Condition> conditions,
            List<Change> changes) {
        this.name = name;
        this.description = description;
        this.restrictive = restrictive;
        this.active = active;
        this.conditions = conditions;
        this.changes = changes;
    }

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
