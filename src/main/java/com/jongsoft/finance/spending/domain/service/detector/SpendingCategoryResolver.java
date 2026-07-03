package com.jongsoft.finance.spending.domain.service.detector;

import static com.jongsoft.finance.banking.types.TransactionLinkType.CATEGORY;
import static com.jongsoft.finance.banking.types.TransactionLinkType.EXPENSE;

import com.jongsoft.finance.banking.domain.model.Transaction;

/** Resolves the spending category name from transaction metadata (EXPENSE preferred, CATEGORY fallback). */
public final class SpendingCategoryResolver {

    private SpendingCategoryResolver() {}

    public static String resolve(Transaction transaction) {
        if (transaction.getMetadata().containsKey(EXPENSE.name())) {
            return transaction.getMetadata().get(EXPENSE.name()).toString();
        }
        if (transaction.getMetadata().containsKey(CATEGORY.name())) {
            return transaction.getMetadata().get(CATEGORY.name()).toString();
        }
        return null;
    }

    public static boolean hasCategory(Transaction transaction) {
        return resolve(transaction) != null;
    }
}
