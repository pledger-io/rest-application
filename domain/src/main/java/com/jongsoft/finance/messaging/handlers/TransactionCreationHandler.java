package com.jongsoft.finance.messaging.handlers;

import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;

public interface TransactionCreationHandler {

    long handleCreatedEvent(CreateTransactionCommand command);

}
