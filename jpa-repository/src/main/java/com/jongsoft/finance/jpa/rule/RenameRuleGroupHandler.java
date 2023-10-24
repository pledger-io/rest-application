package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.RenameRuleGroupCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class RenameRuleGroupHandler implements CommandHandler<RenameRuleGroupCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public RenameRuleGroupHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RenameRuleGroupCommand command) {
        log.info("[{}] - Processing rule group rename event", command.id());

        var hql = """
                update RuleGroupJpa 
                set name = :name
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("name", command.name())
                .execute();
    }

}
