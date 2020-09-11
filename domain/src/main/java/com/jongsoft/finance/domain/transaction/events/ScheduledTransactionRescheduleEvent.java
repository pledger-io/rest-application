package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.domain.core.events.ScheduledRescheduleEvent;
import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;
import lombok.Getter;

import java.util.Map;

@Getter
public class ScheduledTransactionRescheduleEvent extends ScheduledRescheduleEvent {

    private final long id;
    private final Schedulable schedulable;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param id
     * @param schedule
     */
    public ScheduledTransactionRescheduleEvent(Object source, long id, Schedule schedule) {
        super(source, "ScheduledTransaction", schedule);
        this.id = id;

        if (source instanceof Schedulable casted) {
            schedulable = casted;
        } else {
            schedulable = null;
        }
    }

    @Override
    public Schedulable schedulable() {
        return schedulable;
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of("id", id);
    }
}
