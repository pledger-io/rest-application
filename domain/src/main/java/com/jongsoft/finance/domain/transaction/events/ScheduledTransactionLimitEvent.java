package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.domain.core.events.ScheduledLimitEvent;
import com.jongsoft.finance.schedule.Schedulable;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class ScheduledTransactionLimitEvent extends ScheduledLimitEvent {

    private final long id;
    private final Schedulable schedulable;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param id
     * @param start
     * @param end
     */
    public ScheduledTransactionLimitEvent(Object source, long id, LocalDate start, LocalDate end) {
        super(source, "ScheduledTransaction", start, end);
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
