package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.schedule.Schedulable;

import java.util.Map;

public interface ScheduleCommand extends ApplicationEvent {

    String processDefinition();
    Schedulable schedulable();

    default String businessKey() {
        return "bk_" + processDefinition() + "_" + schedulable().getId();
    }

    default Map<String, Object> variables() {
        return Map.of();
    }

}
