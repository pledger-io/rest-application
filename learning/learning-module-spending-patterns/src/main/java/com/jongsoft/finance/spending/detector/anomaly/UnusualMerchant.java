package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class UnusualMerchant implements Anomaly {
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

            return Optional.of(SpendingInsight.builder()
                    .type(InsightType.UNUSUAL_MERCHANT)
                    .category(getExpense(transaction))
                    .severity(Severity.INFO)
                    .score(score)
                    .detectedDate(LocalDate.now())
                    .message("computed.insight.merchant.unusual")
                    .transactionId(transaction.getId())
                    .metadata(Map.of(
                            "merchant", merchant, "known_merchants_count", typicalMerchants.size()))
                    .build());
        }

        return Optional.empty();
    }
}
