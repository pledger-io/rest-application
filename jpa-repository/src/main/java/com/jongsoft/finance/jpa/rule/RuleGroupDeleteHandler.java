package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.commands.rule.RuleGroupDeleteCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class RuleGroupDeleteHandler {

    private static final Logger log = LoggerFactory.getLogger(RuleGroupDeleteHandler.class);

    private final ReactiveEntityManager entityManager;

    RuleGroupDeleteHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @BusinessEventListener
    void handle(RuleGroupDeleteCommand command) {
        log.info("[{}] - Processing rule group delete event", command.id());

        entityManager.update(RuleGroupJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }

}
