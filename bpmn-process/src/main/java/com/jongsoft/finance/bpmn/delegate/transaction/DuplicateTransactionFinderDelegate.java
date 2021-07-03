package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.collection.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DuplicateTransactionFinderDelegate implements JavaDelegate {

    private final TransactionProvider transactionProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariableLocal("transactionId")) {
            var id = execution.<LongValue>getVariableLocalTyped("transactionId").getValue();
            Transaction transaction = transactionProvider.lookup(id)
                    .getOrThrow(() -> new IllegalStateException("Unable to find transaction with id " + id));

            var amount = transaction.computeAmount(transaction.computeFrom());

            List<Transaction> matches = transactionProvider.similar(
                    new EntityRef(transaction.computeFrom().getId()),
                    new EntityRef(transaction.computeTo().getId()),
                    amount,
                    transaction.getDate())
                    .reject(t -> t.getId().equals(id));

            if (!matches.isEmpty()) {
                log.warn("Marking potential duplicate transaction {}", transaction);
                transaction.registerFailure(FailureCode.POSSIBLE_DUPLICATE);
            }
        }
    }

}
