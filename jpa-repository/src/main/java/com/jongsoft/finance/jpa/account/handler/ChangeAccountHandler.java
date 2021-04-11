package com.jongsoft.finance.jpa.account.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeAccountCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangeAccountHandler implements CommandHandler<ChangeAccountCommand> {

    private final ReactiveEntityManager entityManager;

    public ChangeAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeAccountCommand command) {
        log.trace("[{}] - Processing account change event", command.id());

        var hql = """
                update AccountJpa
                set iban = :iban,
                    bic = :bic,
                    number = :number
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("iban", command.iban())
                .set("bic", command.bic())
                .set("number", command.number())
                .set("id", command.id())
                .update();
    }

}
