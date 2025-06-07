package com.jongsoft.finance.domain;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;
import com.jongsoft.lang.Collections;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class FinTrack {

  @Getter private final Encoder hashingAlgorithm;

  public FinTrack(Encoder hashingAlgorithm) {
    this.hashingAlgorithm = hashingAlgorithm;
  }

  public UserAccount createUser(String username, String password) {
    return new UserAccount(username, password);
  }

    public UserAccount createOathUser(String username, String oathKey, List<String> roles) {
        return new UserAccount(username, oathKey, Collections.List(roles));
    }

  public void registerToken(String username, String token, Integer expiresIn) {
    RegisterTokenCommand.tokenRegistered(
        username, token, LocalDateTime.now().plusSeconds(expiresIn));
  }
}
