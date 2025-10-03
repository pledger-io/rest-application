package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.lang.Collections;

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
    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;

    AccountFetcherController(AccountProvider accountProvider, FilterFactory filterFactory) {
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.logger = LoggerFactory.getLogger(AccountFetcherController.class);
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        logger.info("Fetching bank account {}.", id);
        var bankAccount = lookupAccountOrThrow(id);

        return AccountMapper.toAccountResponse(bankAccount);
    }

    @Override
    public PagedAccountResponse getAccounts(
            Integer offset,
            Integer numberOfResults,
            List<@NotNull String> type,
            String accountName) {
        logger.info("Fetching all bank accounts, with provided filters.");

        var page = offset / numberOfResults;
        var filter = filterFactory.account().page(Math.max(0, page), Math.max(1, numberOfResults));
        if (type != null && !type.isEmpty()) {
            filter.types(Collections.List(type));
        }
        if (accountName != null) {
            filter.name(accountName, false);
        }

        var accountResults = accountProvider.lookup(filter);

        return new PagedAccountResponse(
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
        return List.of();
    }

    private Account lookupAccountOrThrow(Long id) {
        var bankAccount =
                accountProvider
                        .lookup(id)
                        .getOrThrow(() -> StatusException.notFound("Bank account is not found"));

        if (bankAccount.isRemove()) {
            throw StatusException.gone("Bank account has been removed from the system");
        }
        return bankAccount;
    }
}
