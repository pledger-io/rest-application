package com.jongsoft.finance.bpmn.delegate.scheduler;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GenerateTransactionJsonDelegate implements JavaDelegate {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final StorageService storageService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var scheduledTransactionId = execution.<LongValue>getVariableLocalTyped("id").getValue();
        var isoDate = execution.<StringValue>getVariableLocalTyped("scheduled").getValue();

        transactionScheduleProvider.lookup(scheduledTransactionId)
                .ifPresent(schedule -> {
                    var transaction = new ParsedTransaction(
                            schedule.getAmount(),
                            Transaction.Type.CREDIT,
                            generateTransactionDescription(schedule),
                            LocalDate.parse(isoDate),
                            null,
                            null,
                            "",
                            "");

                    var transactionToken = storageService.store(transaction.stringify().getBytes(StandardCharsets.UTF_8));

                    execution.setVariable("destinationId", schedule.getDestination().getId());
                    execution.setVariable("sourceId", schedule.getSource().getId());
                    execution.setVariable("transactionToken", transactionToken);
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
