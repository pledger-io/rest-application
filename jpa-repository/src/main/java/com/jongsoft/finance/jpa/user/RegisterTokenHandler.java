package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class RegisterTokenHandler implements CommandHandler<RegisterTokenCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public RegisterTokenHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(RegisterTokenCommand command) {
    log.info("[{}] - Registering new security token.", command.username());

    var userAccountJpa = entityManager
        .from(UserAccountJpa.class)
        .fieldEq("username", command.username())
        .singleResult()
        .get();

    var refreshJpa = AccountTokenJpa.builder()
        .user(userAccountJpa)
        .refreshToken(command.refreshToken())
        .expires(command.expireDate())
        .build();

    entityManager.persist(refreshJpa);
  }
}
