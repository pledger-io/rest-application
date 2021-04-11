package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.messaging.commands.currency.CurrencyCommandType;
import com.jongsoft.finance.providers.CurrencyProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

public class CurrencyProviderJpaIT extends JpaTestSetup {

    @Inject
    private CurrencyProvider currencyProvider;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    void init() {
        loadDataset(
                "sql/base-setup.sql"
        );
    }

    @Test
    void lookup_all() {
        init();
        var check = currencyProvider.lookup();

        Assertions.assertThat(check).hasSize(3);
    }

    @Test
    void lookup_eur() {
        init();
        var check = currencyProvider.lookup("EUR").blockingGet();

        Assertions.assertThat(check.getCode()).isEqualTo("EUR");
        Assertions.assertThat(check.getName()).isEqualTo("Euro");
    }

    @Test
    void handleCreate() {
        init();
        var check = currencyProvider.lookup("MST")
                .isEmpty().blockingGet();
        Assertions.assertThat(check).isTrue();

        eventPublisher.publishEvent(new CreateCurrencyCommand(
                "Mistral",
                'D',
                "MST"
        ));

        var currency = currencyProvider.lookup("MST").blockingGet();
        Assertions.assertThat(currency.getName()).isEqualTo("Mistral");
        Assertions.assertThat(currency.getCode()).isEqualTo("MST");
        Assertions.assertThat(currency.isEnabled()).isTrue();
        Assertions.assertThat(currency.getDecimalPlaces()).isEqualTo(2);
    }

    @Test
    void handleProperty_decimalPlaces() {
        init();
        eventPublisher.publishEvent(new ChangeCurrencyPropertyCommand<>(
                "EUR",
                12,
                CurrencyCommandType.DECIMAL_PLACES
        ));

        var check = currencyProvider.lookup("EUR").blockingGet();
        Assertions.assertThat(check.getDecimalPlaces()).isEqualTo(12);
    }


    @Test
    void handleProperty_enable() {
        init();
        eventPublisher.publishEvent(new ChangeCurrencyPropertyCommand<>(
                "EUR",
                false,
                CurrencyCommandType.ENABLED
        ));

        var check = currencyProvider.lookup("EUR").blockingGet();
        Assertions.assertThat(check.isEnabled()).isFalse();
    }

}
