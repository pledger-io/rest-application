package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeAccountCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeAccountHandler implements CommandHandler<ChangeAccountCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(ChangeAccountCommand command) {
        log.info("[{}] - Processing account change event", command.id());

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
                .execute();
    }

}
