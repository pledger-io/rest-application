package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

@Slf4j
@Singleton
public class CreateTransactionDelegate implements JavaDelegate {

    private final StorageService storageService;
    private final AccountProvider accountProvider;
    private final TransactionCreationHandler creationHandler;

    CreateTransactionDelegate(
            StorageService storageService,
            AccountProvider accountProvider,
            TransactionCreationHandler creationHandler) {
        this.storageService = storageService;
        this.accountProvider = accountProvider;
        this.creationHandler = creationHandler;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String batchImportSlug = (String) execution.getVariableLocal("batchImportSlug");
        String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();
        Account toAccount = accountProvider.lookup(execution.<LongValue>getVariableLocalTyped("accountId").getValue()).get();
        Account targetAccount = accountProvider.lookup(execution.<LongValue>getVariableLocalTyped("targetAccount").getValue()).get();

        ParsedTransaction parsedTransaction = ParsedTransaction.parse(storageService.read(transactionToken).block());
        log.debug("{}: Creating transaction into {} from {} with amount {}",
                execution.getCurrentActivityName(),
                targetAccount.getName(),
                toAccount.getName(),
                parsedTransaction.getAmount());

        Transaction transaction = targetAccount.createTransaction(
                toAccount,
                parsedTransaction.getAmount(),
                parsedTransaction.getType(),
                t -> t.currency(targetAccount.getCurrency())
                        .date(parsedTransaction.getTransactionDate())
                        .bookDate(parsedTransaction.getBookDate())
                        .interestDate(parsedTransaction.getInterestDate())
                        .description(parsedTransaction.getDescription())
                        .importSlug(batchImportSlug));

        long transactionId = creationHandler.handleCreatedEvent(new CreateTransactionCommand(transaction));

        execution.setVariable("transactionId", transactionId);
    }

}
