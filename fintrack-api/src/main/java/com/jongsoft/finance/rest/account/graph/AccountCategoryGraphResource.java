package com.jongsoft.finance.rest.account.graph;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.graph.CategoryPieChart;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.lang.API;
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

@Tag(name = "Account information")
@Controller("/api/accounts/{id}/transactions/graph/category")
public class AccountCategoryGraphResource extends CategoryPieChart {

    private final AccountProvider accountProvider;
    private final CurrencyProvider currencyProvider;

    public AccountCategoryGraphResource(
            FilterFactory filterFactory,
            AccountProvider accountProvider,
            CategoryProvider categoryService,
            TransactionProvider transactionService, CurrencyProvider currencyProvider) {
        super(filterFactory, transactionService, categoryService);

        this.accountProvider = accountProvider;
        this.currencyProvider = currencyProvider;
    }

    @Get("expenses/{start}/{end}")
    @Operation(
            summary = "Generate an account category expense pie chart",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "The account identifier", schema = @Schema(implementation = Long.class)),
                    @Parameter(name = "start", in = ParameterIn.PATH, description = "The start date", schema = @Schema(implementation = LocalDate.class)),
                    @Parameter(name = "end", in = ParameterIn.PATH, description = "The end date", schema = @Schema(implementation = LocalDate.class))
            }
    )
    Highchart expenses(
            @PathVariable long id,
            @PathVariable LocalDate start,
            @PathVariable LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            Principal principal) {
        var account = accountProvider.lookup(id)
                .filter(a -> a.getUser().getUsername().equals(principal.getName()))
                .get();

        var currency = currencySymbol(account.getCurrency());
        return createChart(currencySymbol(account.getCurrency()), locale)
                .addSeries(createSeries(API.List(account), start, end, locale, false, currency));
    }

    @Get("income/{start}/{end}")
    @Operation(
            summary = "Generate an account category income pie chart",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "The account identifier", schema = @Schema(implementation = Long.class)),
                    @Parameter(name = "start", in = ParameterIn.PATH, description = "The start date", schema = @Schema(implementation = LocalDate.class)),
                    @Parameter(name = "end", in = ParameterIn.PATH, description = "The end date", schema = @Schema(implementation = LocalDate.class))
            }
    )
    Highchart income(
            @PathVariable long id,
            @PathVariable LocalDate start,
            @PathVariable LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            Principal principal) {
        var account = accountProvider.lookup(id)
                .filter(a -> a.getUser().getUsername().equals(principal.getName()))
                .get();

        var currency = currencySymbol(account.getCurrency());
        return createChart(currencySymbol(account.getCurrency()), locale)
                .addSeries(createSeries(API.List(account), start, end, locale, true, currency));
    }

    private Currency currencySymbol(String code) {
        return currencyProvider.lookup(code)
                .getOrSupply(() -> null);
    }
}
