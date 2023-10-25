package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.finance.rest.model.CurrencyResponse;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

        Assertions.assertThat(subject.available())
                .hasSize(3)
                .extracting(CurrencyResponse::getCode)
                .containsExactly("EUR", "USD", "KWS");
    }

    @Test
    void create() {
        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        when(currencyProvider.lookup("TCC")).thenReturn(Control.Option());

        Assertions.assertThat(subject.create(request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "TCC")
                .hasFieldOrPropertyWithValue("name", "Test currency")
                .hasFieldOrPropertyWithValue("symbol", 'S');

        verify(applicationEventPublisher).publishEvent(Mockito.any(CreateCurrencyCommand.class));
    }

    @Test
    void get() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        Assertions.assertThat(subject.get("EUR"))
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "EUR")
                .hasFieldOrPropertyWithValue("name", "Euro");
    }

    @Test
    void update() {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        var request = CurrencyRequest.builder()
                .code("TCC")
                .symbol('S')
                .name("Test currency")
                .build();

        Assertions.assertThat(subject.update("EUR", request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "TCC")
                .hasFieldOrPropertyWithValue("name", "Test currency")
                .hasFieldOrPropertyWithValue("symbol", 'S');

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

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        var request = CurrencyPatchRequest.builder()
                .enabled(false)
                .decimalPlaces(2)
                .build();

        Assertions.assertThat(subject.patch("EUR", request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "EUR")
                .hasFieldOrPropertyWithValue("name", "Euro")
                .hasFieldOrPropertyWithValue("enabled", false)
                .hasFieldOrPropertyWithValue("numberDecimals", 2);

        verify(currency).disable();
        verify(currency).accuracy(2);
    }
}
