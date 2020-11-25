package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CurrencyResourceTest {

    private CurrencyResource subject;

    @Mock
    private CurrencyProvider currencyProvider;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new CurrencyResource(currencyProvider);

        new EventBus(applicationEventPublisher);
    }

    @Test
    void available() {
        Mockito.when(currencyProvider.lookup()).thenReturn(
                Collections.List(
                        Currency.builder()
                                .id(1L)
                                .name("Euro")
                                .code("EUR")
                                .symbol('E')
                                .build(),
                        Currency.builder()
                                .id(2L)
                                .name("Dollar")
                                .code("USD")
                                .symbol('D')
                                .build(),
                        Currency.builder()
                                .id(3L)
                                .name("Kwatsch")
                                .code("KWS")
                                .symbol('K')
                                .build()
                ));

        var response = subject.available()
                .test();

        response.assertComplete();
        response.assertValueCount(3);
    }

    @Test
    void create() {
        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        Mockito.when(currencyProvider.lookup("TCC")).thenReturn(Maybe.empty());

        var response = subject.create(request).blockingGet();

        Assertions.assertThat(response.getCode()).isEqualTo("TCC");
        Assertions.assertThat(response.getName()).isEqualTo("Test currency");
        Assertions.assertThat(response.getSymbol()).isEqualTo('S');

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(CurrencyCreatedEvent.class));
    }

    @Test
    void get() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        Mockito.when(currencyProvider.lookup("EUR")).thenReturn(Maybe.just(currency));

        var response = subject.get("EUR").blockingGet();
        Assertions.assertThat(response.getCode()).isEqualTo("EUR");
        Assertions.assertThat(response.getName()).isEqualTo("Euro");
    }

    @Test
    void update() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        Mockito.when(currencyProvider.lookup("EUR")).thenReturn(Maybe.just(currency));

        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        var response = subject.update("EUR", request).blockingGet();
        Assertions.assertThat(response.getCode()).isEqualTo("TCC");
        Assertions.assertThat(response.getName()).isEqualTo("Test currency");
        Assertions.assertThat(response.getSymbol()).isEqualTo('S');

        Mockito.verify(currency).rename("Test currency", "TCC", 'S');
    }

    @Test
    void patch() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .enabled(true)
                .decimalPlaces(3)
                .name("Euro")
                .code("EUR")
                .build());

        Mockito.when(currencyProvider.lookup("EUR")).thenReturn(Maybe.just(currency));

        var request = CurrencyPatchRequest.builder()
                .enabled(false)
                .decimalPlaces(2)
                .build();

        var response = subject.patch("EUR", request).blockingGet();
        Assertions.assertThat(response.isEnabled()).isFalse();
        Assertions.assertThat(response.getNumberDecimals()).isEqualTo(2);

        Mockito.verify(currency).disable();
        Mockito.verify(currency).accuracy(2);
    }
}
