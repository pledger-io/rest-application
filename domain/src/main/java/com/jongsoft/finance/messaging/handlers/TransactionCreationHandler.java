package com.jongsoft.finance.messaging.handlers;

import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;

public interface TransactionCreationHandler {

    /**
     * Handle the creation of a transaction
     *
     * @param command the command
     * @return the id of the created transaction
     */
    long handleCreatedEvent(CreateTransactionCommand command);
}
