package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateRuleGroupHandler implements CommandHandler<CreateRuleGroupCommand> {

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  @Inject
  public CreateRuleGroupHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(CreateRuleGroupCommand command) {
    log.info("[{}] - Processing rule group create event", command.name());

    var currentMax = entityManager
        .from(RuleGroupJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("archived", false)
        .projectSingleValue(Integer.class, "max(sort)");

    var jpaEntity = RuleGroupJpa.builder()
        .name(command.name())
        .user(entityManager.currentUser())
        .sort(currentMax.getOrSupply(() -> 1))
        .build();

    entityManager.persist(jpaEntity);
  }
}
