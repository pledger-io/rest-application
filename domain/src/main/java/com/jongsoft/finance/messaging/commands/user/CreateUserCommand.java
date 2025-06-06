package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateUserCommand(String username, String password) implements ApplicationEvent {

  public static void userCreated(String username, String password) {
    new CreateUserCommand(username, password).publish();
  }
}
