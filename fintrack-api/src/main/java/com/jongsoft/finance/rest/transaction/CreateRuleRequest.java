package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import io.micronaut.core.annotation.Introspected;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CreateRuleRequest {

    @Introspected
    @NoArgsConstructor
    public static class Condition {
        private Long id;

        @NotNull
        private RuleColumn column;

        @NotNull
        private RuleOperation operation;

        @NotBlank
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
        private Long id;
        @NotNull
        private RuleColumn column;
        @NotBlank
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
    private String name;

    @Size(max = 1024)
    private String description;

    private boolean restrictive;
    private boolean active;

    @NotNull
    @Size(min = 1)
    private List<Condition> conditions;

    @NotNull
    @Size(min = 1)
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
