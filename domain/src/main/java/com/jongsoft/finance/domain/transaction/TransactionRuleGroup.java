package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.RenameRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
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

    @BusinessMethod
    public void changeOrder(int sort) {
        if (sort != this.sort) {
            this.sort = sort;
            EventBus.getBus().send(new ReorderRuleGroupCommand(id, sort));
        }
    }

    @BusinessMethod
    public void rename(String name) {
        if (!this.name.equalsIgnoreCase(name)) {
            this.name = name;
            EventBus.getBus().send(new RenameRuleGroupCommand(id, name));
        }
    }

}
