package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.Schedulable;

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
