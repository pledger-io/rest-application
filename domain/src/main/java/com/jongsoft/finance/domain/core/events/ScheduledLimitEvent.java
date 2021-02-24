package com.jongsoft.finance.domain.core.events;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public abstract class ScheduledLimitEvent extends SchedulerEvent {

    private final LocalDate start;
    private final LocalDate end;

    protected ScheduledLimitEvent(Object source, String processDefinition, LocalDate start, LocalDate end) {
        super(source, processDefinition);
        this.start = start;
        this.end = end;
    }

}
