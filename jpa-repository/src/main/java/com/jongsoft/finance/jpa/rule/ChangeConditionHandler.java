package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ChangeConditionCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ChangeConditionHandler implements CommandHandler<ChangeConditionCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeConditionHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeConditionCommand command) {
        log.info("[{}] - Updating rule condition", command.id());

        entityManager.update(RuleConditionJpa.class)
                .set("operation", command.operation())
                .set("condition", command.condition())
                .set("field", command.field())
                .fieldEq("id", command.id())
                .execute();
    }

}
