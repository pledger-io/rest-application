package com.jongsoft.finance.domain.core.events;

import java.util.Map;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.schedule.Schedulable;

public abstract class SchedulerEvent implements ApplicationEvent {

    private final String processDefinition;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param processDefinition
     */
    protected SchedulerEvent(Object source, String processDefinition) {
        this.processDefinition = processDefinition;
    }

    public String getProcessDefinition() {
        return processDefinition;
    }

    public String getBusinessKey() {
        return "bk_" + processDefinition;
    }

    public Map<String, Object> variables() {
        return Map.of();
    }

    public abstract Schedulable schedulable();
}
