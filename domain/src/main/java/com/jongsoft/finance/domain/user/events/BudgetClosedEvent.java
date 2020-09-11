package com.jongsoft.finance.domain.user.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class BudgetClosedEvent implements ApplicationEvent {

    private final long id;
    private final LocalDate endDate;

    public BudgetClosedEvent(Object source, long id, LocalDate endDate) {
        this.id = id;
        this.endDate = endDate;
    }

}
