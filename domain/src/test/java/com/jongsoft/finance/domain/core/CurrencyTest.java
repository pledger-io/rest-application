package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.messaging.commands.currency.CurrencyCommandType;
import com.jongsoft.finance.messaging.commands.currency.RenameCurrencyCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CurrencyTest {

    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        new EventBus(eventPublisher);
    }

    @Test
    void newTest() {
        new Currency("Euro", "EUR", 'E');

        var captor = ArgumentCaptor.forClass(CreateCurrencyCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().isoCode()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().name()).isEqualTo("Euro");
        Assertions.assertThat(captor.getValue().symbol()).isEqualTo('E');
    }

    @Test
    void rename() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .symbol('E')
                .build();

        currency.rename("Dollar", "USD", 'U');

        var captor = ArgumentCaptor.forClass(RenameCurrencyCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().isoCode()).isEqualTo("USD");
        Assertions.assertThat(captor.getValue().name()).isEqualTo("Dollar");
        Assertions.assertThat(captor.getValue().symbol()).isEqualTo('U');
    }

    @Test
    void enable() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .enabled(false)
                .symbol('E')
                .build();

        currency.enable();

        var captor = ArgumentCaptor.forClass(ChangeCurrencyPropertyCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().code()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().value()).isEqualTo(true);
        Assertions.assertThat(captor.getValue().type()).isEqualTo(CurrencyCommandType.ENABLED);
    }

    @Test
    void enable_alreadyEnabled() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .enabled(true)
                .symbol('E')
                .build();

        currency.enable();

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(ChangeCurrencyPropertyCommand.class);
    }

    @Test
    void disable() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .enabled(true)
                .symbol('E')
                .build();

        currency.disable();

        var captor = ArgumentCaptor.forClass(ChangeCurrencyPropertyCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().code()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().value()).isEqualTo(false);
        Assertions.assertThat(captor.getValue().type()).isEqualTo(CurrencyCommandType.ENABLED);
    }

    @Test
    void disable_alreadyDisabled() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .enabled(false)
                .symbol('E')
                .build();

        currency.disable();

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(ChangeCurrencyPropertyCommand.class);
    }

    @Test
    void accuracy() {
        var currency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .decimalPlaces(2)
                .enabled(false)
                .symbol('E')
                .build();

        currency.accuracy(12);

        var captor = ArgumentCaptor.forClass(ChangeCurrencyPropertyCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().code()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().value()).isEqualTo(12);
        Assertions.assertThat(captor.getValue().type()).isEqualTo(CurrencyCommandType.DECIMAL_PLACES);
    }
}
