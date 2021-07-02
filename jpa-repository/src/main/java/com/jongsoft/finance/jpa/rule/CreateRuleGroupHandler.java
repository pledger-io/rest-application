package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateRuleGroupHandler implements CommandHandler<CreateRuleGroupCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(CreateRuleGroupCommand command) {
        log.info("[{}] - Processing rule group create event", command.name());

        var hql = """
                select max(sort) + 1 from RuleGroupJpa
                where user.username = :username and archived = false""";

        var currentMax = entityManager
                .<Integer>blocking()
                .hql(hql)
                .set("username", authenticationFacade)
                .maybe();

        var jpaEntity = RuleGroupJpa.builder()
                .name(command.name())
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
                .sort(currentMax.getOrSupply(() -> 1))
                .build();

        entityManager.persist(jpaEntity);
    }

}
