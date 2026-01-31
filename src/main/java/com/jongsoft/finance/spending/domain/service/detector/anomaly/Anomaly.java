package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static com.jongsoft.finance.banking.types.TransactionLinkType.EXPENSE;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import java.util.Optional;

public interface Anomaly {

    Optional<SpendingInsight> detect(Transaction transaction, UserCategoryStatistics statistics);

    default Severity getSeverityFromScore(double score) {
        if (score >= 0.8) {
            return Severity.ALERT;
        } else if (score >= 0.5) {
            return Severity.WARNING;
        } else {
            return Severity.INFO;
        }
    }

    default String getExpense(Transaction transaction) {
        return transaction.getMetadata().get(EXPENSE.name()).toString();
    }
}
