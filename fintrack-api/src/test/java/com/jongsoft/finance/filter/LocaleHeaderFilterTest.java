package com.jongsoft.finance.filter;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.filter.ServerFilterChain;

class LocaleHeaderFilterTest {

    private LocaleHeaderFilter subject = new LocaleHeaderFilter();

    @Test
    void doFilterOnce() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var headers = Mockito.mock(HttpHeaders.class);

        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        Mockito.doReturn(Optional.of("en")).when(headers).get(HttpHeaders.ACCEPT_LANGUAGE, String.class);

        subject.doFilterOnce(mockRequest, Mockito.mock(ServerFilterChain.class));

        Mockito.verify(mockRequest).setAttribute(RequestAttributes.LOCALIZATION, Locale.forLanguageTag("en"));
    }

    @Test
    void doFilterOnce_noLocalization() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var headers = Mockito.mock(HttpHeaders.class);

        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        Mockito.doReturn(Optional.empty()).when(headers).get(HttpHeaders.ACCEPT_LANGUAGE, String.class);

        subject.doFilterOnce(mockRequest, Mockito.mock(ServerFilterChain.class));

        Mockito.verify(mockRequest, Mockito.never()).setAttribute(Mockito.eq(RequestAttributes.LOCALIZATION), Mockito.any());
    }
}
