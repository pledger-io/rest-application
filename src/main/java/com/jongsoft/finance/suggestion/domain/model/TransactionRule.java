package com.jongsoft.finance.suggestion.domain.model;

import com.jongsoft.finance.suggestion.domain.commands.ChangeConditionCommand;
import com.jongsoft.finance.suggestion.domain.commands.ChangeRuleCommand;
import com.jongsoft.finance.suggestion.domain.commands.ReorderRuleCommand;
import com.jongsoft.finance.suggestion.domain.commands.RuleRemovedCommand;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.finance.suggestion.types.RuleOperation;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.List;

import java.io.Serializable;
import java.util.Objects;

public class TransactionRule {

    public class Condition implements Serializable {
        private Long id;
        private RuleColumn field;
        private RuleOperation operation;
        private String condition;

        public Condition(Long id, RuleColumn field, RuleOperation operation, String condition) {
            this.id = id;
            this.field = field;
            this.operation = operation;
            this.condition = condition;

            conditions = conditions.append(this);
        }

        public void update(RuleColumn field, RuleOperation operation, String condition) {
            var hasChanged = Control.Equal(this.field, field)
                    .append(this.operation, operation)
                    .append(this.condition, condition)
                    .isNotEqual();

            if (hasChanged) {
                this.field = field;
                this.operation = operation;
                this.condition = condition;

                ChangeConditionCommand.changeConditionUpdated(id, field, operation, condition);
            }
        }

        public void delete() {
            conditions = conditions.reject(c -> Objects.equals(c.getId(), id));
        }

        public Long getId() {
            return id;
        }

        public RuleColumn getField() {
            return field;
        }

        public RuleOperation getOperation() {
            return operation;
        }

        public String getCondition() {
            return condition;
        }
    }

    public class Change implements Serializable {
        private Long id;
        private RuleColumn field;
        private String change;

        public Change(Long id, RuleColumn field, String change) {
            this.id = id;
            this.field = field;
            this.change = change;

            changes = changes.append(this);
        }

        public void update(RuleColumn ruleColumn, String change) {
            var hasChanged = Control.Equal(ruleColumn, this.field)
                    .append(change, this.change)
                    .isNotEqual();

            if (hasChanged) {
                this.field = ruleColumn;
                this.change = change;

                ChangeRuleCommand.changeRuleUpdated(id, ruleColumn, change);
            }
        }

        public void delete() {
            changes = changes.reject(r -> Objects.equals(r.getId(), id));
        }

        public Long getId() {
            return id;
        }

        public RuleColumn getField() {
            return field;
        }

        public String getChange() {
            return change;
        }
    }

    private Long id;
    private String name;
    private String description;
    private boolean restrictive;
    private boolean active;
    private boolean deleted;
    private String group;
    private int sort;

    private List<Condition> conditions;
    private List<Change> changes;

    private TransactionRule(String name, boolean restrictive) {
        this.name = name;
        this.restrictive = restrictive;
        this.conditions = Collections.List();
        this.changes = Collections.List();
    }

    public TransactionRule(
            Long id,
            String name,
            String description,
            boolean restrictive,
            boolean active,
            boolean deleted,
            String group,
            int sort,
            List<Condition> conditions,
            List<Change> changes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.restrictive = restrictive;
        this.active = active;
        this.deleted = deleted;
        this.group = group;
        this.sort = sort;
        this.conditions = Control.Option(conditions).getOrSupply(Collections::List);
        this.changes = Control.Option(changes).getOrSupply(Collections::List);
    }

    public void change(String name, String description, boolean restrictive, boolean active) {
        this.name = name;
        this.restrictive = restrictive;
        this.active = active;
        this.description = description;
    }

    public void changeOrder(int sort) {
        if (this.sort != sort) {
            this.sort = sort;
            if (id != null) {
                ReorderRuleCommand.reorderRuleUpdated(id, sort);
            }
        }
    }

    public void assign(String ruleGroup) {
        this.group = ruleGroup;
    }

    public void remove() {
        deleted = true;
        RuleRemovedCommand.ruleRemoved(id);
    }

    public void registerCondition(RuleColumn field, RuleOperation operation, String condition) {
        if (conditions == null) {
            conditions = Collections.List();
        }

        new Condition(null, field, operation, condition);
    }

    public void registerChange(RuleColumn field, String value) {
        if (changes == null) {
            changes = Collections.List();
        }

        changes = changes.reject(c -> Objects.equals(c.getField(), field));
        new Change(null, field, value);
    }

    public Condition findCondition(long conditionId) {
        return this.conditions
                .first(c -> Objects.equals(c.getId(), conditionId))
                .get();
    }

    public Change findChange(long changeId) {
        return this.changes.first(c -> Objects.equals(c.getId(), changeId)).get();
    }

    public Long getId() {
        return id;
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

    public boolean isDeleted() {
        return deleted;
    }

    public String getGroup() {
        return group;
    }

    public int getSort() {
        return sort;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public static TransactionRule create(String name, boolean restrictive) {
        return new TransactionRule(name, restrictive);
    }
}
