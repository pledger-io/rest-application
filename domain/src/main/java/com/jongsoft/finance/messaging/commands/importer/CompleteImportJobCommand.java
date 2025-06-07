package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CompleteImportJobCommand(long id) implements ApplicationEvent {

  public static void importJobCompleted(long id) {
    new CompleteImportJobCommand(id).publish();
  }
}
