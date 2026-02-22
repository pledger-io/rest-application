package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.finance.spending.types.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class UnusualMerchant implements Anomaly {

    private final Logger log = LoggerFactory.getLogger(UnusualMerchant.class);

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var merchant = transaction.computeTo().getName();
        var typicalMerchants = statistics.typicalMerchants().get(getExpense(transaction));
        if (typicalMerchants == null || typicalMerchants.isEmpty()) {
            log.trace(
                    "Not enough data for transaction {}. Skipping anomaly detection.",
                    transaction.getId());
            return Optional.empty();
        }

        if (!typicalMerchants.contains(merchant)) {
            double score = 0.8; // Fixed score for unusual merchant

            return Optional.of(new SpendingInsight(
                    InsightType.UNUSUAL_MERCHANT,
                    getExpense(transaction),
                    Severity.INFO,
                    score,
                    transaction.getId(),
                    transaction.getDate(),
                    "computed.insight.merchant.unusual",
                    Map.of(
                            "merchant",
                            merchant,
                            "known_merchants_count",
                            typicalMerchants.size())));
        }

        return Optional.empty();
    }
}
