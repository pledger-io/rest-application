package com.jongsoft.finance.messaging.handlers;

import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;

public interface TransactionCreationHandler {

    long handleCreatedEvent(TransactionCreatedEvent event);

}
