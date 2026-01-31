package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.AccountTypeProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.core.adapter.api.SettingProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.AccountFetcherApi;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Controller
class AccountFetcherController implements AccountFetcherApi {

    private final Logger logger;
    private final AccountTypeProvider accountTypeProvider;
    private final AccountProvider accountProvider;
    private final FilterProvider<AccountProvider.FilterCommand> filterFactory;
    private final SettingProvider settingProvider;

    AccountFetcherController(
            AccountTypeProvider accountTypeProvider,
            AccountProvider accountProvider,
            FilterProvider<AccountProvider.FilterCommand> filterFactory,
            SettingProvider settingProvider) {
        this.accountTypeProvider = accountTypeProvider;
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.settingProvider = settingProvider;
        this.logger = LoggerFactory.getLogger(AccountFetcherController.class);
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        logger.info("Fetching bank account {}.", id);
        var bankAccount = lookupAccountOrThrow(id);

        return AccountMapper.toAccountResponse(bankAccount);
    }

    @Override
    public AccountPagedResponse getAccounts(
            Integer offset,
            Integer numberOfResults,
            List<@NotNull String> type,
            String accountName) {
        logger.info("Fetching all bank accounts, with provided filters.");

        var page = offset / numberOfResults;
        var filter = filterFactory.create().page(Math.max(0, page), Math.max(1, numberOfResults));
        if (!type.isEmpty()) {
            filter.types(Collections.List(type));
        } else {
            filter.types(accountTypeProvider.lookup(false));
        }
        if (accountName != null) {
            filter.name(accountName, false);
        }

        var accountResults = accountProvider.lookup(filter);

        return new AccountPagedResponse(
                new PagedResponseInfo(
                        accountResults.total(), accountResults.pages(), accountResults.pageSize()),
                accountResults.content().map(AccountMapper::toAccountResponse).toJava());
    }

    @Override
    public List<@Valid SavingGoalResponse> getSavingGoalsForAccount(Long id) {
        logger.info("Fetching saving goals for bank account {}.", id);
        var bankAccount = lookupAccountOrThrow(id);

        return bankAccount.getSavingGoals().map(AccountMapper::toSavingGoalResponse).stream()
                .toList();
    }

    @Override
    public List<@Valid AccountSpendingResponse> getTopAccountsBySpending(
            LocalDate startDate, LocalDate endDate, AccountType type, Boolean useOwnAccounts) {
        logger.info("Fetching top accounts by spending.");

        var filter = filterFactory.create().page(0, settingProvider.getAutocompleteLimit());
        switch (type) {
            case DEBIT -> filter.types(Collections.List("debtor"));
            case CREDITOR -> filter.types(Collections.List("creditor"));
            default -> throw StatusException.badRequest("Invalid account type");
        }

        var ascending =
                switch (type) {
                    case DEBIT -> true;
                    case CREDITOR -> false;
                };

        return accountProvider
                .top(filter, Dates.range(startDate, endDate), ascending)
                .map(this::toAccountSpendingResponse)
                .toJava();
    }

    private Account lookupAccountOrThrow(Long id) {
        Account bankAccount = accountProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Bank account is not found"));

        if (bankAccount.isRemove()) {
            throw StatusException.gone("Bank account has been removed from the system");
        }
        return bankAccount;
    }

    private AccountSpendingResponse toAccountSpendingResponse(
            AccountProvider.AccountSpending accountSpending) {
        return new AccountSpendingResponse(
                AccountMapper.toAccountResponse(accountSpending.account()),
                accountSpending.average(),
                accountSpending.total());
    }
}
