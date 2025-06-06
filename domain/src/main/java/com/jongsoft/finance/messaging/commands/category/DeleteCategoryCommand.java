package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DeleteCategoryCommand(long id) implements ApplicationEvent {

  public static void categoryDeleted(long id) {
    new DeleteCategoryCommand(id).publish();
  }
}
