package com.jongsoft.finance.suggestion.adapter.api;

import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;
import com.jongsoft.finance.suggestion.domain.model.TransactionResult;

import java.util.Optional;

public interface SuggestionEngine {

    SuggestionResult makeSuggestions(SuggestionInput transactionInput);

    /**
     * Extracts a transaction based on the given input string and returns a {@code
     * TransactionResult}.
     *
     * @param transactionInput the raw transaction input string to be processed
     * @return a {@code TransactionResult} containing the extracted transaction data
     */
    default Optional<TransactionResult> extractTransaction(String transactionInput) {
        return Optional.empty();
    }
}
