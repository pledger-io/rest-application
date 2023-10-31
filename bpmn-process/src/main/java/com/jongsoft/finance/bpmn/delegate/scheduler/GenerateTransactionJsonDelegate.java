package com.jongsoft.finance.bpmn.delegate.scheduler;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Singleton
public class GenerateTransactionJsonDelegate implements JavaDelegate {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final StorageService storageService;
    private final ProcessMapper mapper;

    GenerateTransactionJsonDelegate(
            TransactionScheduleProvider transactionScheduleProvider,
            StorageService storageService,
            ProcessMapper mapper) {
        this.transactionScheduleProvider = transactionScheduleProvider;
        this.storageService = storageService;
        this.mapper = mapper;
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

                    var transactionToken = storageService.store(mapper.writeSafe(transaction).getBytes(StandardCharsets.UTF_8));

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
