package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.RuleRemovedCommand;
import com.jongsoft.finance.security.AuthenticationFacade;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class RuleRemovedHandler implements CommandHandler<RuleRemovedCommand> {

    private final Logger logger;
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    RuleRemovedHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.logger = LoggerFactory.getLogger(RuleRemovedHandler.class);
    }

    @Override
    @BusinessEventListener
    public void handle(RuleRemovedCommand command) {
        logger.info("[{}] - Processing rule delete event.", command.ruleId());

        entityManager
                .update(RuleJpa.class)
                .set("archived", true)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("id", command.ruleId())
                .execute();
    }
}
