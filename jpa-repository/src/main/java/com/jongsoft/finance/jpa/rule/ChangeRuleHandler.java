package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ChangeRuleCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeRuleHandler implements CommandHandler<ChangeRuleCommand> {

    private final ReactiveEntityManager entityManager;

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
