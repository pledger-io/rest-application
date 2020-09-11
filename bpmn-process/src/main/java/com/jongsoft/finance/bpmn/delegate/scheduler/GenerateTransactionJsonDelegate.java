package com.jongsoft.finance.bpmn.delegate.scheduler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionScheduleProvider;

@Singleton
public class GenerateTransactionJsonDelegate implements JavaDelegate {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final StorageService storageService;

    public GenerateTransactionJsonDelegate(
            TransactionScheduleProvider transactionScheduleProvider,
            StorageService storageService) {
        this.transactionScheduleProvider = transactionScheduleProvider;
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var scheduledTransactionId = execution.<LongValue>getVariableLocalTyped("id").getValue();
        var isoDate = execution.<StringValue>getVariableLocalTyped("scheduled").getValue();

        var schedule = transactionScheduleProvider.lookup()
                .filter(e -> Objects.equals(e.getId(), scheduledTransactionId))
                .head();

        var transaction = new ParsedTransaction(
                schedule.getAmount(),
                Transaction.Type.CREDIT,
                schedule.getDescription(),
                LocalDate.parse(isoDate),
                null,
                null,
                "",
                "");

        var transactionToken = storageService.store(transaction.stringify().getBytes(StandardCharsets.UTF_8));

        execution.setVariable("destinationId", schedule.getDestination().getId());
        execution.setVariable("sourceId", schedule.getSource().getId());
        execution.setVariable("transactionToken", transactionToken);
    }

}
