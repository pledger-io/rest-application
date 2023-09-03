package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ChangeRuleCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ChangeRuleHandler implements CommandHandler<ChangeRuleCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeRuleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeRuleCommand command) {
        log.info("[{}] - Updating rule change", command.id());

        var hql = """
                update %s
                set field = :field,
                    value = :value
                where id = :id""".formatted(RuleChangeJpa.class.getName());

        entityManager.update()
                .hql(hql)
                .set("field", command.column())
                .set("value", command.change())
                .set("id", command.id())
                .execute();
    }

}
