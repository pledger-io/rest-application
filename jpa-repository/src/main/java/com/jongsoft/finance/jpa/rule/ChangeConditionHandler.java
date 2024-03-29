package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
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

        String hql = """
                update %s
                set field = :field,
                    operation = :operation,
                    condition = :condition
                where id = :id""".formatted(RuleConditionJpa.class.getName());

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("operation", command.operation())
                .set("condition", command.condition())
                .set("field", command.field())
                .execute();
    }

}
