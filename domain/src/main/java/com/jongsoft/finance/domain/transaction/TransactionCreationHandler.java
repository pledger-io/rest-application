package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;

public interface TransactionCreationHandler {

    long handleCreatedEvent(TransactionCreatedEvent event);

}
