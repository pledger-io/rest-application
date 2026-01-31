package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;

public interface TransactionCreationHandler {

    /**
     * Handle the creation of a transaction
     *
     * @param command the command
     * @return the id of the created transaction
     */
    long handleCreatedEvent(CreateTransactionCommand command);
}
