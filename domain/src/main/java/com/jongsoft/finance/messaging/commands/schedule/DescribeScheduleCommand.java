package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DescribeScheduleCommand(long id, String description, String name)
    implements ApplicationEvent {

  public static void scheduleDescribed(long id, String description, String name) {
    new DescribeScheduleCommand(id, description, name).publish();
  }
}
