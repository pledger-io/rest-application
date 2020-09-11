package com.jongsoft.finance.domain.core.events;

import com.jongsoft.finance.schedule.Schedule;
import lombok.Getter;

@Getter
public abstract class ScheduledRescheduleEvent extends SchedulerEvent {

    private final Schedule schedule;

    public ScheduledRescheduleEvent(Object source, String processDefinition, Schedule schedule) {
        super(source, processDefinition);
        this.schedule = schedule;
    }

}
