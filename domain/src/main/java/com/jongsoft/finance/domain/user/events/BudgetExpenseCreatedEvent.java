package com.jongsoft.finance.domain.user.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class BudgetExpenseCreatedEvent implements ApplicationEvent {

    private final String name;
    private final LocalDate start;
    private final double lowerBound;
    private final double upperBound;

    public BudgetExpenseCreatedEvent(Object source, String name, LocalDate start, double lowerBound, double upperBound) {
        this.name = name;
        this.start = start;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

}
