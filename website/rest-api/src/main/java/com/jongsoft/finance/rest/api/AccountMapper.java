package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.rest.model.*;

interface AccountMapper {

    static AccountResponse toAccountResponse(Account account) {
        var accountNumbers = new AccountResponseAllOfAccount();
        var response =
                new AccountResponse(
                        account.getId(), account.getName(), account.getType(), accountNumbers);

        response.setDescription(account.getDescription());
        if (account.getInterestPeriodicity() != null) {
            response.setInterest(
                    new AccountResponseAllOfInterest(
                            Periodicity.fromValue(account.getInterestPeriodicity().name()),
                            account.getInterest()));
        }
        if (account.getFirstTransaction() != null) {
            response.setHistory(
                    new AccountResponseAllOfHistory(
                            account.getFirstTransaction(), account.getLastTransaction()));
        }

        accountNumbers.setBic(account.getBic());
        accountNumbers.setIban(account.getIban());
        accountNumbers.setNumber(account.getNumber());
        accountNumbers.setCurrency(account.getCurrency());

        return response;
    }
}
