package com.jongsoft.finance.rest.transaction.graph;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.graph.CategoryPieChart;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.lang.API;
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
    Highchart expenses(
            @PathVariable LocalDate start,
            @PathVariable LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Currency currency) {
        currency = currencyCode(currency);
        return createChart(currency, locale)
                .addSeries(createSeries(API.List(), start, end, locale, false, currency));
    }

    @Get("income/{start}/{end}")
    Highchart income(
            @PathVariable LocalDate start,
            @PathVariable LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Currency currency) {

        currency = currencyCode(currency);
        return createChart(currencyCode(currency), locale)
                .addSeries(createSeries(API.List(), start, end, locale, true, currency));
    }

    private Currency currencyCode(Currency currency) {
        return API.Option(currency)
                .getOrSupply(this::fallbackCurrency);
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
