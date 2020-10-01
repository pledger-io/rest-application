package com.jongsoft.finance.rest.account.graph;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.graph.BudgetPieChart;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.lang.API;
import io.micronaut.context.MessageSource;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.RequestAttribute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Locale;

@Tag(name = "Graph Generation")
@Controller("/api/accounts/{id}/transactions/graph/budget/{start}/{end}")
public class AccountBudgetGraphResource extends BudgetPieChart {

    private final AccountProvider accountProvider;
    private final CurrencyProvider currencyProvider;

    public AccountBudgetGraphResource(
            MessageSource messageSource,
            FilterFactory filterFactory,
            AccountProvider accountProvider,
            TransactionProvider transactionService,
            BudgetProvider budgetService, CurrencyProvider currencyProvider) {
        super(messageSource, filterFactory, transactionService, budgetService);
        this.accountProvider = accountProvider;
        this.currencyProvider = currencyProvider;
    }

    @Get
    @Operation(
            summary = "Account Budget Graph",
            description = "Generate an account budget pie chart",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "The account identifier", schema = @Schema(implementation = Long.class)),
                    @Parameter(name = "start", in = ParameterIn.PATH, description = "The start date", schema = @Schema(implementation = LocalDate.class)),
                    @Parameter(name = "end", in = ParameterIn.PATH, description = "The end date", schema = @Schema(implementation = LocalDate.class))
            }
    )
    Highchart budget(
            @PathVariable long id,
            @PathVariable @Format("yyyy-MM-dd") LocalDate start,
            @PathVariable @Format("yyyy-MM-dd") LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            Principal principal) {
        var account = accountProvider.lookup(id)
                .filter(a -> a.getUser().getUsername().equals(principal.getName()))
                .get();

        return createChart(currencySymbol(account.getCurrency()), locale)
                .addSeries(createSeries(API.List(account), start, end, locale));
    }

    private String currencySymbol(String code) {
        return currencyProvider.lookup(code)
                .map(Currency::getSymbol)
                .map(String::valueOf)
                .blockingGet("");
    }

}
