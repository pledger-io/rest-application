package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.SavingGoal;
import com.jongsoft.finance.rest.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

interface AccountMapper {

    static AccountLink toAccountLink(Account account) {
        return new AccountLink(account.getId(), account.getName(), account.getType());
    }

    static AccountResponse toAccountResponse(Account account) {
        var accountNumbers = new AccountResponseAllOfAccount();
        var response = new AccountResponse(
                account.getId(), account.getName(), account.getType(), accountNumbers);

        response.setDescription(account.getDescription());
        if (account.getInterestPeriodicity() != null) {
            response.setInterest(new AccountResponseAllOfInterest(
                    Periodicity.fromValue(account.getInterestPeriodicity().name()),
                    account.getInterest()));
        }
        if (account.getFirstTransaction() != null) {
            response.setHistory(new AccountResponseAllOfHistory(
                    account.getFirstTransaction(), account.getLastTransaction()));
        }

        accountNumbers.setBic(account.getBic());
        accountNumbers.setIban(account.getIban());
        accountNumbers.setNumber(account.getNumber());
        accountNumbers.setCurrency(account.getCurrency());

        return response;
    }

    static SavingGoalResponse toSavingGoalResponse(SavingGoal savingGoal) {
        var response = new SavingGoalResponse(
                savingGoal.getId(),
                savingGoal.getGoal(),
                savingGoal.getAllocated(),
                savingGoal.getTargetDate());

        response.setName(savingGoal.getName());
        response.setDescription(savingGoal.getDescription());
        if (savingGoal.getSchedule() != null) {
            response.setSchedule(new ScheduleResponse(
                    Periodicity.fromValue(savingGoal.getSchedule().periodicity().name()),
                    savingGoal.getSchedule().interval()));
            response.setInstallments(savingGoal.computeAllocation());
        }
        response.setMonthsLeft(Math.max(
                ChronoUnit.MONTHS.between(LocalDate.now(), savingGoal.getTargetDate()), 0));

        return response;
    }
}
