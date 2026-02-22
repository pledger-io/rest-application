package com.jongsoft.finance.banking.domain.service;

import com.jongsoft.finance.banking.adapter.api.TransactionAi;
import com.jongsoft.finance.banking.domain.model.TransactionResult;
import com.jongsoft.finance.banking.domain.service.ai.TransactionExtractorAgent;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;

import io.micrometer.core.annotation.Timed;

import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.UUID;

@Singleton
class TransactionAiService implements TransactionAi {
    private final TransactionExtractorAgent transactionExtractorAgent;

    TransactionAiService(TransactionExtractorAgent transactionExtractorAgent) {
        this.transactionExtractorAgent = transactionExtractorAgent;
    }

    @Override
    @Timed(
            value = "learning.language-model",
            extraTags = {"action", "extract-transaction"})
    public Optional<TransactionResult> extractTransaction(String text) {
        var extracted = transactionExtractorAgent.extractTransaction(
                UUID.randomUUID(), LocalDate.now(), text);
        return Control.Option(new TransactionResult(
                extracted.type(),
                extracted.date(),
                Control.Option(extracted.fromAccount())
                        .map(e -> new TransactionResult.AccountResult(
                                Control.Option(e.id()).getOrSupply(() -> -1L), e.name()))
                        .getOrSupply(() -> null),
                Control.Option(extracted.toAccount())
                        .map(e -> new TransactionResult.AccountResult(
                                Control.Option(e.id()).getOrSupply(() -> -1L), e.name()))
                        .getOrSupply(() -> null),
                extracted.description(),
                extracted.amount()));
    }
}
