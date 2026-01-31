package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.SavingGoal;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.rest.AccountCommandApi;
import com.jongsoft.finance.rest.model.*;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Predicate;

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

        Account.create(
                currentUserProvider.currentUser().getUsername(),
                accountRequest.getName(),
                accountRequest.getCurrency(),
                accountRequest.getType());

        var bankAccount = accountProvider
                .lookup(accountRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create bank account"));

        updateBankAccount(bankAccount, accountRequest);

        return HttpResponse.created(AccountMapper.toAccountResponse(bankAccount));
    }

    @Override
    public HttpResponse<@Valid SavingGoalResponse> createSavingGoalForAccount(
            Long id, SavingGoalRequest savingGoalRequest) {
        logger.info("Creating saving goal for bank account {}.", id);
        var savingGoal = lookupAccountOrThrow(id)
                .createSavingGoal(
                        savingGoalRequest.getName(),
                        savingGoalRequest.getGoal(),
                        savingGoalRequest.getTargetDate());

        var createdGoal = lookupAccountOrThrow(id)
                .getSavingGoals()
                .first(goal -> Objects.equals(savingGoal.getName(), goal.getName()))
                .getOrThrow(() ->
                        StatusException.internalError("Could not locate created saving goal"));
        createdGoal.schedule(Periodicity.MONTHS, 1);
        return HttpResponse.created(AccountMapper.toSavingGoalResponse(createdGoal));
    }

    @Override
    public HttpResponse<Void> deleteAccountById(Long id) {
        logger.info("Deleting bank account {} for user.", id);

        lookupAccountOrThrow(id).terminate();

        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> deleteSavingGoalForAccount(Long id, Long goalId) {
        logger.info("Deleting saving goal {} for bank account {}.", goalId, id);
        var bankAccount = lookupAccountOrThrow(id);

        lookupSavingGoalOrThrow(goalId, bankAccount).completed();

        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> makeReservationForSavingGoal(
            Long id, Long goalId, SavingReservationRequest savingReservationRequest) {
        logger.info("Making reservation for saving goal {} for bank account {}.", goalId, id);

        var bankAccount = lookupAccountOrThrow(id);
        var savingGoal = lookupSavingGoalOrThrow(goalId, bankAccount);

        savingGoal.registerPayment(BigDecimal.valueOf(savingReservationRequest.getAmount()));

        return HttpResponse.noContent();
    }

    @Override
    public AccountResponse updateAccountById(Long id, AccountRequest accountRequest) {
        logger.info("Updating bank account {}.", id);
        var bankAccount = lookupAccountOrThrow(id);

        updateBankAccount(bankAccount, accountRequest);

        return AccountMapper.toAccountResponse(bankAccount);
    }

    @Override
    public SavingGoalResponse updateSavingGoalForAccount(
            Long id, Long goalId, SavingGoalRequest savingGoalRequest) {
        logger.info("Updating saving goal {} for bank account {}.", goalId, id);
        var bankAccount = lookupAccountOrThrow(id);
        var savingGoal = lookupSavingGoalOrThrow(goalId, bankAccount);
        savingGoal.adjustGoal(savingGoalRequest.getGoal(), savingGoalRequest.getTargetDate());
        return AccountMapper.toSavingGoalResponse(savingGoal);
    }

    private void updateBankAccount(Account bankAccount, AccountRequest accountRequest) {
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
    }

    private Account lookupAccountOrThrow(Long id) {
        return accountProvider
                .lookup(id)
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.notFound("Bank account is not found"));
    }

    private SavingGoal lookupSavingGoalOrThrow(Long id, Account account) {
        return account.getSavingGoals()
                .first(goal -> Objects.equals(goal.getId(), id))
                .getOrThrow(() -> StatusException.notFound("Saving goal is not found"));
    }
}
