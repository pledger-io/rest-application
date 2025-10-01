package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AccountCommandController implements AccountCommandApi {

    private final Logger logger;
    private final AccountProvider accountProvider;
    private final CurrentUserProvider currentUserProvider;

    public AccountCommandController(
            AccountProvider accountProvider, CurrentUserProvider currentUserProvider) {
        this.accountProvider = accountProvider;
        this.currentUserProvider = currentUserProvider;
        this.logger = LoggerFactory.getLogger(AccountCommandController.class);
    }

    @Override
    public HttpResponse<@Valid AccountResponse> createAccount(AccountRequest accountRequest) {
        logger.info("Creating bank account for user.");
        accountProvider
                .lookup(accountRequest.getName())
                .ifPresent(() -> StatusException.badRequest("Bank account already exists"));

        currentUserProvider
                .currentUser()
                .createAccount(
                        accountRequest.getName(),
                        accountRequest.getCurrency(),
                        accountRequest.getType());

        var bankAccount =
                accountProvider
                        .lookup(accountRequest.getName())
                        .getOrThrow(
                                () ->
                                        StatusException.internalError(
                                                "Failed to create bank account"));

        if (accountRequest.getDescription() != null) {
            bankAccount.rename(
                    accountRequest.getName(),
                    accountRequest.getDescription(),
                    accountRequest.getCurrency(),
                    accountRequest.getType());
        }

        if (accountRequest.getInterest() != null) {
            bankAccount.interest(
                    accountRequest.getInterest(),
                    Periodicity.valueOf(accountRequest.getInterestPeriodicity().name()));
        }

        bankAccount.changeAccount(
                accountRequest.getIban(), accountRequest.getBic(), accountRequest.getNumber());

        return HttpResponse.created(AccountMapper.toAccountResponse(bankAccount));
    }

    @Override
    public HttpResponse<@Valid SavingGoalResponse> createSavingGoalForAccount(
            Long id, SavingGoalRequest savingGoalRequest) {
        return null;
    }

    @Override
    public HttpResponse<Void> deleteAccountById(Long id) {
        return null;
    }

    @Override
    public HttpResponse<Void> deleteSavingGoalForAccount(Long id, Long goalId) {
        return null;
    }

    @Override
    public HttpResponse<Void> makeReservationForSavingGoal(
            Long id, Long goalId, SavingReservationRequest savingReservationRequest) {
        return null;
    }

    @Override
    public AccountResponse updateAccountById(Long id) {
        return null;
    }

    @Override
    public SavingGoalResponse updateSavingGoalForAccount(
            Long id, Long goalId, SavingGoalRequest savingGoalRequest) {
        return null;
    }
}
