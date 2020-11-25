package com.jongsoft.finance.rest.transaction.graph;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.graph.CategoryPieChart;
import com.jongsoft.finance.rest.DateFormat;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.MessageSource;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

@Tag(name = "Graph Generation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/transactions/graphs/category")
public class TransactionCategoryGraphResource extends CategoryPieChart {

    private final CurrentUserProvider currentUserProvider;
    private final CurrencyProvider currencyProvider;

    public TransactionCategoryGraphResource(
            MessageSource messageSource,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            CategoryProvider categoryProvider,
            CurrentUserProvider currentUserProvider,
            CurrencyProvider currencyProvider) {
        super(messageSource, filterFactory, transactionProvider, categoryProvider);
        this.currentUserProvider = currentUserProvider;
        this.currencyProvider = currencyProvider;
    }

    @Get("expenses/{start}/{end}")
    String expenses(
            @PathVariable @DateFormat LocalDate start,
            @PathVariable @DateFormat LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Optional<Currency> currency) {
        var useCurrency = currency.orElseGet(this::fallbackCurrency);

        return createChart(useCurrency, locale)
                .addSeries(createSeries(Collections.List(), start, end, locale, false, useCurrency))
                .toJson();
    }

    @Get("income/{start}/{end}")
    String income(
            @PathVariable @DateFormat LocalDate start,
            @PathVariable @DateFormat LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Optional<Currency> currency) {
        var useCurrency = currency.orElseGet(this::fallbackCurrency);

        return createChart(useCurrency, locale)
                .addSeries(createSeries(Collections.List(), start, end, locale, true, useCurrency))
                .toJson();
    }

    private Currency fallbackCurrency() {
        var currencyCode = currentUserProvider.currentUser()
                .getPrimaryCurrency()
                .getCurrencyCode();

        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Single.error(StatusException.badRequest("Currency not located")))
                .blockingGet();
    }

}
