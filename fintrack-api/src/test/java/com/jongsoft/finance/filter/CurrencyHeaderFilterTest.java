package com.jongsoft.finance.filter;

import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Maybe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class CurrencyHeaderFilterTest {

    private CurrencyProvider currencyProvider;
    private CurrencyHeaderFilter subject;

    @BeforeEach
    void setup() {
        currencyProvider = Mockito.mock(CurrencyProvider.class);
        subject = new CurrencyHeaderFilter(currencyProvider);
    }

    @Test
    void doFilterOnce() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var headers = Mockito.mock(HttpHeaders.class);
        var currency = Currency.builder()
                .id(2L)
                .code("USD")
                .build();

        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        Mockito.doReturn(Optional.of("USD")).when(headers).get("X-Accept-Currency", String.class);
        Mockito.doReturn(Maybe.just(currency)).when(currencyProvider).lookup("USD");

        subject.doFilterOnce(mockRequest, Mockito.mock(ServerFilterChain.class));

        Mockito.verify(mockRequest).setAttribute(RequestAttributes.CURRENCY, currency);
    }

    @Test
    void doFilterOnce_missingCurrency() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var headers = Mockito.mock(HttpHeaders.class);

        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        Mockito.doReturn(Optional.of("USD")).when(headers).get("X-Accept-Currency", String.class);
        Mockito.doReturn(Maybe.empty()).when(currencyProvider).lookup("USD");

        subject.doFilterOnce(mockRequest, Mockito.mock(ServerFilterChain.class));

        Mockito.verify(mockRequest, Mockito.never()).setAttribute(Mockito.eq(RequestAttributes.CURRENCY), Mockito.any());
    }
}
