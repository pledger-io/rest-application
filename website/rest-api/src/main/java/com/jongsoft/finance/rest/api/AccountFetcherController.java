package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.model.*;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Controller
class AccountFetcherController implements AccountFetcherApi {

    private final AccountProvider accountProvider;

    AccountFetcherController(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        return accountProvider
                .lookup(id)
                .map(AccountMapper::toAccountResponse)
                .getOrThrow(() -> StatusException.notFound("Bank account is not found"));
    }

    @Override
    public PagedAccountResponse getAccounts(
            Integer offset,
            Integer numberOfResults,
            List<@NotNull String> type,
            String accountName) {
        return null;
    }

    @Override
    public List<@Valid SavingGoalResponse> getSavingGoalsForAccount(Long id) {
        return List.of();
    }

    @Override
    public List<@Valid AccountSpendingResponse> getTopAccountsBySpending(
            LocalDate startDate, LocalDate endDate, AccountType type, Boolean useOwnAccounts) {
        return List.of();
    }
}
