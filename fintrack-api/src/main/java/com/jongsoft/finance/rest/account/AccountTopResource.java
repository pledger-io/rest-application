package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.DateFormat;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@ApiDefaults
@Controller("/api/accounts/top")
@Tag(name = "Account information")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class AccountTopResource {

    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;
    private final SettingProvider settingProvider;

    public AccountTopResource(
            AccountProvider accountProvider,
            FilterFactory filterFactory,
            SettingProvider settingProvider) {
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.settingProvider = settingProvider;
    }


    @Get("/debit/{start}/{end}")
    @Operation(
            summary = "Top debit accounts",
            description = "Calculates and returns the accounts where you spent the most for the given date range",
            operationId = "listTopDebtors"
    )
    List<AccountSpendingResponse> topDebtors(
            @PathVariable @DateFormat LocalDate start,
            @PathVariable @DateFormat LocalDate end) {
        var filterCommand = filterFactory.account()
                .types(Collections.List("debtor"))
                .pageSize(settingProvider.getAutocompleteLimit());

        return accountProvider.top(filterCommand, Dates.range(start, end), true)
                .map(AccountSpendingResponse::new)
                .toJava();
    }

    @Get("/creditor/{start}/{end}")
    @Operation(
            summary = "Top creditor accounts",
            description = "Calculates and returns the accounts that credited the most money for the given date range",
            operationId = "listTopCreditors"
    )
    List<AccountSpendingResponse> topCreditor(
            @PathVariable @DateFormat LocalDate start,
            @PathVariable @DateFormat LocalDate end) {
        var filterCommand = filterFactory.account()
                .types(Collections.List("creditor"))
                .pageSize(settingProvider.getAutocompleteLimit());

        return accountProvider.top(filterCommand, Dates.range(start, end), false)
                .map(AccountSpendingResponse::new)
                .toJava();
    }

}
