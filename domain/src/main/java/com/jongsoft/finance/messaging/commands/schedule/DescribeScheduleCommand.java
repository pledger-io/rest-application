package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.core.ApplicationEvent;

public record DescribeScheduleCommand(long id, String description, String name) implements ApplicationEvent {
}
