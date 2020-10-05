package com.jongsoft.finance.rest.transaction.graph;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.graph.BudgetPieChart;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.MessageSource;
import io.micronaut.core.convert.format.Format;
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
@Controller("/api/transactions/graphs/budget/{start}/{end}")
public class TransactionBudgetGraphResource extends BudgetPieChart {

    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;
    private final CurrentUserProvider currentUserProvider;
    private final AccountTypeProvider accountTypeProvider;
    private final CurrencyProvider currencyProvider;

    public TransactionBudgetGraphResource(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            BudgetProvider budgetProvider,
            AccountProvider accountProvider,
            CurrentUserProvider currentUserProvider,
            MessageSource messageSource,
            AccountTypeProvider accountTypeProvider,
            CurrencyProvider currencyProvider) {
        super(messageSource, filterFactory, transactionProvider, budgetProvider);
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.currentUserProvider = currentUserProvider;
        this.accountTypeProvider = accountTypeProvider;
        this.currencyProvider = currencyProvider;
    }

    @Get
    String budget(
            @PathVariable @Format("yyyy-MM-dd") LocalDate start,
            @PathVariable @Format("yyyy-MM-dd") LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale) {
        var ownAccounts = accountProvider.lookup(filterFactory.account()
                .types(accountTypeProvider.lookup(false)))
                .content();

        return createChart(currencySymbol(), locale)
                .addSeries(createSeries(ownAccounts, start, end, locale))
                .toJson();
    }

    private String currencySymbol() {
        var code = API.Option(currentUserProvider.currentUser())
                .map(UserAccount::getPrimaryCurrency)
                .map(java.util.Currency::getCurrencyCode)
                .getOrSupply(() -> "EUR");

        return currencyProvider.lookup(code)
                .map(Currency::getSymbol)
                .map(String::valueOf)
                .switchIfEmpty(Single.error(StatusException.badRequest("Currency not found")))
                .blockingGet();
    }
}
