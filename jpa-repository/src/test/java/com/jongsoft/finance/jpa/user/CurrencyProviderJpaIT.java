package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.messaging.commands.currency.CurrencyCommandType;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CurrencyProviderJpaIT extends JpaTestSetup {

    @Inject
    private CurrencyProvider currencyProvider;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void init() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql"
        );
    }

    @Test
    void lookup_all() {
        var check = currencyProvider.lookup();

        Assertions.assertThat(check).hasSize(3);
    }

    @Test
    void lookup_eur() {
        var check = currencyProvider.lookup("EUR").get();

        Assertions.assertThat(check.getCode()).isEqualTo("EUR");
        Assertions.assertThat(check.getName()).isEqualTo("Euro");
    }

    @Test
    void handleCreate() {
        var check = currencyProvider.lookup("MST");
        Assertions.assertThat(check).isEmpty();

        eventPublisher.publishEvent(new CreateCurrencyCommand(
                "Mistral",
                'D',
                "MST"
        ));

        var currency = currencyProvider.lookup("MST").get();
        Assertions.assertThat(currency.getName()).isEqualTo("Mistral");
        Assertions.assertThat(currency.getCode()).isEqualTo("MST");
        Assertions.assertThat(currency.isEnabled()).isTrue();
        Assertions.assertThat(currency.getDecimalPlaces()).isEqualTo(2);
    }

    @Test
    void handleProperty_decimalPlaces() {
        eventPublisher.publishEvent(new ChangeCurrencyPropertyCommand<>(
                "EUR",
                12,
                CurrencyCommandType.DECIMAL_PLACES
        ));

        var check = currencyProvider.lookup("EUR").get();
        Assertions.assertThat(check.getDecimalPlaces()).isEqualTo(12);
    }


    @Test
    void handleProperty_enable() {
        eventPublisher.publishEvent(new ChangeCurrencyPropertyCommand<>(
                "EUR",
                false,
                CurrencyCommandType.ENABLED
        ));

        Assertions.assertThat(currencyProvider.lookup("EUR")).first()
                .satisfies(currency -> Assertions.assertThat(currency.isEnabled()).isFalse());
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
