package com.jongsoft.finance.domain.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.events.CurrencyPropertyEvent;
import com.jongsoft.finance.domain.core.events.CurrencyRenameEvent;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

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

        var captor = ArgumentCaptor.forClass(CurrencyCreatedEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getCode()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().getName()).isEqualTo("Euro");
        Assertions.assertThat(captor.getValue().getSymbol()).isEqualTo('E');
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

        var captor = ArgumentCaptor.forClass(CurrencyRenameEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getCode()).isEqualTo("USD");
        Assertions.assertThat(captor.getValue().getName()).isEqualTo("Dollar");
        Assertions.assertThat(captor.getValue().getSymbol()).isEqualTo('U');
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

        var captor = ArgumentCaptor.forClass(CurrencyPropertyEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getCode()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().getValue()).isEqualTo(true);
        Assertions.assertThat(captor.getValue().getType()).isEqualTo(CurrencyPropertyEvent.Type.ENABLED);
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

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(CurrencyPropertyEvent.class);
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

        var captor = ArgumentCaptor.forClass(CurrencyPropertyEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getCode()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().getValue()).isEqualTo(false);
        Assertions.assertThat(captor.getValue().getType()).isEqualTo(CurrencyPropertyEvent.Type.ENABLED);
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

        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(CurrencyPropertyEvent.class);
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

        var captor = ArgumentCaptor.forClass(CurrencyPropertyEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getCode()).isEqualTo("EUR");
        Assertions.assertThat(captor.getValue().getValue()).isEqualTo(12);
        Assertions.assertThat(captor.getValue().getType()).isEqualTo(CurrencyPropertyEvent.Type.DECIMAL_PLACES);
    }
}
