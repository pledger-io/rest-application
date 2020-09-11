package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.schedule.Periodicity;

import lombok.Getter;

@Getter
public class AccountInterestEvent implements ApplicationEvent {

    private final long accountId;
    private final double interest;
    private final Periodicity interestPeriodicity;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param accountId
     * @param interest
     * @param interestPeriodicity
     */
    public AccountInterestEvent(Object source, long accountId, double interest, Periodicity interestPeriodicity) {
        this.accountId = accountId;
        this.interest = interest;
        this.interestPeriodicity = interestPeriodicity;
    }

}
