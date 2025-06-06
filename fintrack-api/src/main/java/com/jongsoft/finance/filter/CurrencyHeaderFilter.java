package com.jongsoft.finance.filter;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.providers.CurrencyProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;

@Filter("/**")
public class CurrencyHeaderFilter implements HttpServerFilter {

  private final Logger log = getLogger(CurrencyHeaderFilter.class);

  private final CurrencyProvider currencyProvider;

  public CurrencyHeaderFilter(CurrencyProvider currencyProvider) {
    this.currencyProvider = currencyProvider;
  }

  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(
      final HttpRequest<?> request, final ServerFilterChain chain) {
    var requestedCurrency = request.getHeaders().get("X-Accept-Currency", String.class);

    if (requestedCurrency.isPresent()) {
      log.debug("Filtering for currency {}", requestedCurrency.get());
      currencyProvider
          .lookup(requestedCurrency.get())
          .ifPresent(curr -> request.setAttribute(RequestAttributes.CURRENCY, curr));
    } else {
      request.setAttribute(RequestAttributes.CURRENCY, "");
    }

    return chain.proceed(request);
  }
}
