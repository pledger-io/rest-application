package com.jongsoft.finance.bpmn.delegate.scheduler;

import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.time.LocalDate;

@Singleton
public class GenerateTransactionJsonDelegate implements JavaDelegate {

    private final TransactionScheduleProvider transactionScheduleProvider;

    GenerateTransactionJsonDelegate(TransactionScheduleProvider transactionScheduleProvider) {
        this.transactionScheduleProvider = transactionScheduleProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var scheduledTransactionId = execution.<LongValue>getVariableLocalTyped("id").getValue();
        var isoDate = execution.<StringValue>getVariableLocalTyped("scheduled").getValue();

        transactionScheduleProvider.lookup(scheduledTransactionId)
                .ifPresent(schedule -> {
                    var transaction = ParsedTransaction.builder()
                            .amount(schedule.getAmount())
                            .type(Transaction.Type.CREDIT)
                            .transactionDate(LocalDate.parse(isoDate))
                            .description(generateTransactionDescription(schedule))
                            .build();


                    execution.setVariable("destinationId", schedule.getDestination().getId());
                    execution.setVariable("sourceId", schedule.getSource().getId());
                    execution.setVariable("transaction", transaction);
                }).elseThrow(() -> new IllegalStateException("Cannot find schedule with id " + scheduledTransactionId));
    }

    private String generateTransactionDescription(ScheduledTransaction scheduledTransaction) {
        var descriptionBuilder = new StringBuilder(scheduledTransaction.getName());
        if (scheduledTransaction.getDescription() != null) {
            descriptionBuilder
                    .append(": ")
                    .append(scheduledTransaction.getDescription());
        }
        return descriptionBuilder.toString();
    }

}
