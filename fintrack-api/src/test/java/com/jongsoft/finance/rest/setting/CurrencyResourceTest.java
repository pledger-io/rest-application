package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(currencyProvider.lookup()).thenReturn(
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

        StepVerifier.create(subject.available())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void create() {
        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        when(currencyProvider.lookup("TCC")).thenReturn(Mono.empty());

        StepVerifier.create(subject.create(request))
                .assertNext(response -> {
                    assertThat(response.getCode()).isEqualTo("TCC");
                    assertThat(response.getName()).isEqualTo("Test currency");
                    assertThat(response.getSymbol()).isEqualTo('S');
                })
                .verifyComplete();


        verify(applicationEventPublisher).publishEvent(Mockito.any(CreateCurrencyCommand.class));
    }

    @Test
    void get() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Mono.just(currency));

        StepVerifier.create(subject.get("EUR"))
                .assertNext(response -> {
                    assertThat(response.getCode()).isEqualTo("EUR");
                    assertThat(response.getName()).isEqualTo("Euro");
                })
                .verifyComplete();
    }

    @Test
    void update() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Mono.just(currency));

        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        StepVerifier.create(subject.update("EUR", request))
                .assertNext(response -> {
                    assertThat(response.getCode()).isEqualTo("TCC");
                    assertThat(response.getName()).isEqualTo("Test currency");
                    assertThat(response.getSymbol()).isEqualTo('S');
                })
                .verifyComplete();

        verify(currency).rename("Test currency", "TCC", 'S');
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

        when(currencyProvider.lookup("EUR")).thenReturn(Mono.just(currency));

        var request = CurrencyPatchRequest.builder()
                .enabled(false)
                .decimalPlaces(2)
                .build();

        StepVerifier.create(subject.patch("EUR", request))
                .assertNext(response -> {
                    assertThat(response.isEnabled()).isFalse();
                    assertThat(response.getNumberDecimals()).isEqualTo(2);
                })
                .verifyComplete();

        verify(currency).disable();
        verify(currency).accuracy(2);
    }
}
