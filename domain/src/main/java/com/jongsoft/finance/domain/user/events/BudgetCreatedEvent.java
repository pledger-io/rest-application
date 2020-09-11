package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.Budget;

import lombok.Getter;

@Getter
public class BudgetCreatedEvent implements ApplicationEvent {

    private final Budget budget;

    public BudgetCreatedEvent(Object source, Budget budget) {
        this.budget = budget;
    }

}
