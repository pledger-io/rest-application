package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.commands.rule.RenameRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.RuleGroupDeleteCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
public class TransactionRuleGroup implements AggregateBase {

    private Long id;
    private String name;
    private int sort;
    private boolean archived;

    @BusinessMethod
    public void changeOrder(int sort) {
        if (sort != this.sort) {
            this.sort = sort;
            ReorderRuleGroupCommand.reorderRuleGroupUpdated(id, sort);
        }
    }

    @BusinessMethod
    public void rename(String name) {
        if (!this.name.equalsIgnoreCase(name)) {
            this.name = name;
            RenameRuleGroupCommand.ruleGroupRenamed(id, name);
        }
    }

    @BusinessMethod
    public void delete() {
        RuleGroupDeleteCommand.ruleGroupDeleted(id);
    }

}
