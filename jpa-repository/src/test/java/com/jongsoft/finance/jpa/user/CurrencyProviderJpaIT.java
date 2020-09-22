package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.events.CurrencyPropertyEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
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
        var check = currencyProvider.lookup("EUR");
        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getCode()).isEqualTo("EUR");
        Assertions.assertThat(check.get().getName()).isEqualTo("Euro");
    }

    @Test
    void handleCreate() {
        init();
        var check = currencyProvider.lookup("MST");
        Assertions.assertThat(check.isPresent()).isFalse();

        eventPublisher.publishEvent(new CurrencyCreatedEvent(
                this,
                "Mistral",
                'D',
                "MST"
        ));

        check = currencyProvider.lookup("MST");
        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("Mistral");
        Assertions.assertThat(check.get().getCode()).isEqualTo("MST");
        Assertions.assertThat(check.get().isEnabled()).isTrue();
        Assertions.assertThat(check.get().getDecimalPlaces()).isEqualTo(2);
    }

    @Test
    void handleProperty_decimalPlaces() {
        init();
        eventPublisher.publishEvent(new CurrencyPropertyEvent<>(
                this,
                "EUR",
                12,
                CurrencyPropertyEvent.Type.DECIMAL_PLACES
        ));

        var check = currencyProvider.lookup("EUR");
        Assertions.assertThat(check.get().getDecimalPlaces()).isEqualTo(12);
    }


    @Test
    void handleProperty_enable() {
        init();
        eventPublisher.publishEvent(new CurrencyPropertyEvent<>(
                this,
                "EUR",
                false,
                CurrencyPropertyEvent.Type.ENABLED
        ));

        var check = currencyProvider.lookup("EUR");
        Assertions.assertThat(check.get().isEnabled()).isFalse();
    }

}
