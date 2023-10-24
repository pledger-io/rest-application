package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ReorderRuleHandler implements CommandHandler<ReorderRuleCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public ReorderRuleHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(ReorderRuleCommand command) {
        log.info("[{}] - Processing transaction rule sort event", command.id());

        var jpaEntity = entityManager.getDetached(RuleJpa.class, Collections.Map("id", command.id()));

        var hql = """
                update %s
                set sort = sort + :direction
                where sort between :fromSort and :untilSort
                 and id in (select id from %s rj
                        where rj.user.username = :username
                            and rj.group.name = :name)"""
                .formatted(
                        RuleJpa.class.getName(),
                        RuleJpa.class.getName());

        var update = entityManager.update()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", jpaEntity.getGroup().getName());

        if ((command.sort() - jpaEntity.getSort()) < 0) {
            update.set("direction", 1)
                    .set("fromSort", command.sort())
                    .set("untilSort", jpaEntity.getSort());
        } else {
            update.set("direction", -1)
                    .set("fromSort", jpaEntity.getSort())
                    .set("untilSort", command.sort());
        }
        update.execute();

        entityManager.update()
                .hql("""
                        update %s
                        set sort = :sort
                        where id = :id""".formatted(RuleJpa.class.getName()))
                .set("sort", command.sort())
                .set("id", command.id())
                .execute();
    }

}
