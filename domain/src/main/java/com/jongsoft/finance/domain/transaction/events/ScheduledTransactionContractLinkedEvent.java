package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

public record ScheduledTransactionContractLinkedEvent(long id, long contractId)
        implements ApplicationEvent {

}
