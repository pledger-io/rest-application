package com.jongsoft.finance.bpmn.delegate.scheduler;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.importer.api.TransactionDTO;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
public class GenerateTransactionJsonDelegate implements JavaDelegate, JavaBean {

  private final TransactionScheduleProvider transactionScheduleProvider;

  GenerateTransactionJsonDelegate(TransactionScheduleProvider transactionScheduleProvider) {
    this.transactionScheduleProvider = transactionScheduleProvider;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var scheduledTransactionId =
        execution.<LongValue>getVariableLocalTyped("id").getValue();
    var isoDate = execution.<StringValue>getVariableLocalTyped("scheduled").getValue();

    transactionScheduleProvider
        .lookup(scheduledTransactionId)
        .ifPresent(schedule -> {
          var transaction = new TransactionDTO(
              schedule.getAmount(),
              TransactionType.CREDIT,
              generateTransactionDescription(schedule),
              LocalDate.parse(isoDate),
              null,
              null,
              schedule.getDestination().getIban(),
              schedule.getDestination().getName(),
              null,
              null,
              List.of());

          execution.setVariable("destinationId", schedule.getDestination().getId());
          execution.setVariable("sourceId", schedule.getSource().getId());
          execution.setVariable("transaction", transaction);
        })
        .elseThrow(() ->
            new IllegalStateException("Cannot find schedule with id " + scheduledTransactionId));
  }

  private String generateTransactionDescription(ScheduledTransaction scheduledTransaction) {
    var descriptionBuilder = new StringBuilder(scheduledTransaction.getName());
    if (scheduledTransaction.getDescription() != null) {
      descriptionBuilder.append(": ").append(scheduledTransaction.getDescription());
    }
    return descriptionBuilder.toString();
  }
}
