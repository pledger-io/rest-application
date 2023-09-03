package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.RenameCurrencyCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RenameCurrencyHandler implements CommandHandler<RenameCurrencyCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public RenameCurrencyHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RenameCurrencyCommand command) {
        log.info("[{}] - Processing currency rename event", command.id());

        var hql = """
                update CurrencyJpa c 
                set c.name = :name,
                    c.code = :code,
                    c.symbol = :symbol
                where c.id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("name", command.name())
                .set("code", command.isoCode())
                .set("symbol", command.symbol())
                .execute();
    }

}
