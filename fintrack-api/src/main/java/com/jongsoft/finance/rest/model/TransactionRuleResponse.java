package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.lang.Collections;

import java.util.List;

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

    public String getDescription() {
        return wrapped.getDescription();
    }

    public boolean isActive() {
        return wrapped.isActive();
    }

    public boolean isRestrictive() {
        return wrapped.isRestrictive();
    }

    public List<Change> getChanges() {
        return Collections.List(wrapped.getChanges()
                .map(Change::new))
                .toJava();
    }

    public List<Condition> getConditions() {
        return Collections.List(wrapped.getConditions())
                .map(Condition::new)
                .toJava();
    }

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
