package com.jongsoft.finance.messaging.commands;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.util.Map;

public record StartProcessCommand(String processDefinition, Map<String, Object> parameters)
    implements ApplicationEvent {

  public static void startProcess(String processDefinition, Map<String, Object> parameters) {
    new StartProcessCommand(processDefinition, parameters).publish();
  }
}
