package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.lang.Collections;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Serdeable.Serializable
public class TransactionRuleResponse {

    private final TransactionRule wrapped;

    public TransactionRuleResponse(TransactionRule wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    @Schema(description = "The description for this transaction rule.")
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Schema(description = "True if the rule is active in the system.")
    public boolean isActive() {
        return wrapped.isActive();
    }

    @Schema(description = "True if the rule terminates the flow of rule execution.")
    public boolean isRestrictive() {
        return wrapped.isRestrictive();
    }

    @Schema(description = "The sort order of the rule.")
    public int getSort() {
        return wrapped.getSort();
    }

    @Schema(
            description =
                    "The changes this rule will apply on any transaction matching the condition.")
    public List<Change> getChanges() {
        return Collections.List(wrapped.getChanges().map(Change::new)).toJava();
    }

    @Schema(description = "The conditions this rule will check for on any given transaction.")
    public List<Condition> getConditions() {
        return Collections.List(wrapped.getConditions()).map(Condition::new).toJava();
    }

    @Serdeable.Serializable
    public static class Change {
        private final TransactionRule.Change wrapped;

        public Change(TransactionRule.Change wrapped) {
            this.wrapped = wrapped;
        }

        public long getId() {
            return wrapped.getId();
        }

        public RuleColumn getField() {
            return wrapped.getField();
        }

        public String getChange() {
            return wrapped.getChange();
        }
    }

    @Serdeable.Serializable
    public static class Condition {
        private final TransactionRule.Condition wrapped;

        public Condition(TransactionRule.Condition wrapped) {
            this.wrapped = wrapped;
        }

        public long getId() {
            return wrapped.getId();
        }

        public RuleColumn getField() {
            return wrapped.getField();
        }

        public RuleOperation getOperation() {
            return wrapped.getOperation();
        }

        public String getCondition() {
            return wrapped.getCondition();
        }
    }
}
