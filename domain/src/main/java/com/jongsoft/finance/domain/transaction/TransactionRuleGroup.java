package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupRenamedEvent;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupSortedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
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
            EventBus.getBus().send(new TransactionRuleGroupSortedEvent(this, id, sort));
        }
    }

    @BusinessMethod
    public void rename(String name) {
        if (!this.name.equalsIgnoreCase(name)) {
            this.name = name;
            EventBus.getBus().send(new TransactionRuleGroupRenamedEvent(this, id, name));
        }
    }

}
