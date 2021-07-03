package com.jongsoft.finance.bpmn.delegate.scheduler;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GenerateTransactionJsonDelegate implements JavaDelegate {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final StorageService storageService;

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
