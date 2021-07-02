package com.jongsoft.finance.jpa.currency;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateCurrencyHandler implements CommandHandler<CreateCurrencyCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(CreateCurrencyCommand command) {
        log.info("[{}] - Processing currency create event", command.isoCode());

        var entity = CurrencyJpa.builder()
                .name(command.name())
                .code(command.isoCode())
                .symbol(command.symbol())
                .enabled(true)
                .decimalPlaces(2)
                .build();

        entityManager.persist(entity);
    }
}
