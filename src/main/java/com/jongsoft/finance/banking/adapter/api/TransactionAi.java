package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.TransactionResult;
import com.jongsoft.lang.control.Optional;

public interface TransactionAi {
    Optional<TransactionResult> extractTransaction(String text);
}
