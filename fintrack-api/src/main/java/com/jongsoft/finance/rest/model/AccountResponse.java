package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Periodicity;

public class AccountResponse {

    private final Account wrapped;

    public AccountResponse(final Account wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public String getType() {
        return wrapped.getType();
    }

    public NumberInformation getAccount() {
        return new NumberInformation();
    }

    public InterestInformation getInterest() {
        return new InterestInformation();
    }

    public class InterestInformation {

        public Periodicity getPeriodicity() {
            return wrapped.getInterestPeriodicity();
        }

        public double getInterest() {
            return wrapped.getInterest();
        }

    }

    public class NumberInformation {

        public String getIban() {
            return wrapped.getIban();
        }

        public String getBic() {
            return wrapped.getBic();
        }

        public String getNumber() {
            return wrapped.getNumber();
        }

        public String getCurrency() {
            return wrapped.getCurrency();
        }
    }
}
