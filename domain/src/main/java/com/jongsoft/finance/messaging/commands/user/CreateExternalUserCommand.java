package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.lang.collection.List;

public record CreateExternalUserCommand(String username, String oauthToken, List<String> roles)
    implements ApplicationEvent {

  public static void externalUserCreated(String username, String oauthToken, List<String> roles) {
    new CreateExternalUserCommand(username, oauthToken, roles).publish();
  }
}
