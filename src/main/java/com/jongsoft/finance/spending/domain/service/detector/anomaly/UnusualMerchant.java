package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.finance.spending.types.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

public class UnusualMerchant implements Anomaly {

    private static final int MIN_TRANSACTIONS_FOR_AMOUNT = 5;

    private final Logger log = LoggerFactory.getLogger(UnusualMerchant.class);

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var merchantAccount = transaction.computeTo();
        if (merchantAccount == null) {
            return Optional.empty();
        }
        var merchant = merchantAccount.getName();
        var category = getExpense(transaction);
        var typicalMerchants = statistics.typicalMerchants().get(category);
        if (typicalMerchants == null || typicalMerchants.isEmpty()) {
            log.trace(
                    "Not enough data for transaction {}. Skipping anomaly detection.",
                    transaction.getId());
            return Optional.empty();
        }

        if (typicalMerchants.contains(merchant)) {
            return Optional.empty();
        }

        if (!isSignificantAmount(transaction, statistics)) {
            return Optional.empty();
        }

        double score = 0.8;
        var metadata = new HashMap<String, Object>();
        metadata.put("merchant", merchant);
        metadata.put("known_merchants_count", typicalMerchants.size());
        metadata.put("baseline_months", statistics.baselineMonths());

        return Optional.of(new SpendingInsight(
                InsightType.UNUSUAL_MERCHANT,
                category,
                Severity.INFO,
                score,
                transaction.getId(),
                transaction.getDate(),
                "computed.insight.merchant.unusual",
                metadata));
    }

    private boolean isSignificantAmount(
            Transaction transaction, UserCategoryStatistics statistics) {
        double transactionAmount = transaction.computeAmount(transaction.computeTo());
        var typicalAmount = statistics.amounts().get(getExpense(transaction));
        if (typicalAmount == null || typicalAmount.getN() < MIN_TRANSACTIONS_FOR_AMOUNT) {
            return transactionAmount > 0;
        }

        return transactionAmount > typicalAmount.getMean();
    }
}
