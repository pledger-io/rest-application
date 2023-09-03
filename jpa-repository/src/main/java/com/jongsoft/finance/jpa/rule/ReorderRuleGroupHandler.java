package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReorderRuleGroupHandler implements CommandHandler<ReorderRuleGroupCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public ReorderRuleGroupHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(ReorderRuleGroupCommand command) {
        log.info("[{}] - Processing rule group sorting event", command.id());

        var jpaEntity = entityManager.getDetached(RuleGroupJpa.class, Collections.Map("id", command.id()));

        var hql = """
                update RuleGroupJpa
                set sort = sort + :direction
                where sort between :fromSort and :untilSort
                 and id in (select id from RuleGroupJpa rj where rj.user.username = :username)""";

        var pipeline = entityManager.update()
                .hql(hql)
                .set("username", authenticationFacade.authenticated());

        if ((command.sort() - jpaEntity.getSort()) < 0) {
            pipeline.set("direction", 1)
                    .set("fromSort", command.sort())
                    .set("untilSort", jpaEntity.getSort());
        } else {
            pipeline.set("direction", -1)
                    .set("fromSort", jpaEntity.getSort())
                    .set("untilSort", command.sort());
        }
        pipeline.execute();

        entityManager.update()
                .hql("update RuleGroupJpa set sort = :sort where id = :id")
                .set("id", command.id())
                .set("sort", command.sort())
                .execute();
    }

}
