package com.jongsoft.finance.llm;

import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.learning.TransactionResult;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.agent.TransactionExtractorAgent;
import com.jongsoft.finance.llm.stores.ClassificationEmbeddingStore;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Primary
@Singleton
@AiEnabled
class AiSuggestionEngine implements SuggestionEngine {

    private final Logger log = getLogger(AiSuggestionEngine.class);

    private final ClassificationEmbeddingStore embeddingStore;
    private final ClassificationAgent classificationAgent;
    private final TransactionExtractorAgent transactionExtractorAgent;

    public AiSuggestionEngine(
            ClassificationEmbeddingStore embeddingStore,
            ClassificationAgent classificationAgent,
            TransactionExtractorAgent transactionExtractorAgent) {
        this.embeddingStore = embeddingStore;
        this.classificationAgent = classificationAgent;
        this.transactionExtractorAgent = transactionExtractorAgent;
    }

    @Override
    public SuggestionResult makeSuggestions(SuggestionInput transactionInput) {
        log.debug("Starting classification on {}.", transactionInput);
        var nullSafeInput = new SuggestionInput(
                transactionInput.date(),
                Optional.ofNullable(transactionInput.description()).orElse(""),
                Optional.ofNullable(transactionInput.fromAccount()).orElse(""),
                Optional.ofNullable(transactionInput.toAccount()).orElse(""),
                transactionInput.amount());

        var suggestions = embeddingStore.classify(nullSafeInput)
                .orElseGet(() -> fallbackToLLM(nullSafeInput));

        log.trace("Finished classification with suggestions {}.", suggestions);
        return suggestions;
    }

    @Override
    public Optional<TransactionResult> extractTransaction(String transactionInput) {
        var extracted = transactionExtractorAgent.extractTransaction(UUID.randomUUID(), transactionInput);
        return Optional.of(new TransactionResult(
                extracted.type(),
                extracted.date(),
                Optional.ofNullable(extracted.fromAccount())
                        .map(e -> new TransactionResult.AccountResult(Optional.ofNullable(e.id()).orElse(-1L), e.name()))
                        .orElse(null),
                Optional.ofNullable(extracted.toAccount())
                        .map(e -> new TransactionResult.AccountResult(Optional.ofNullable(e.id()).orElse(-1L), e.name()))
                        .orElse(null),
                extracted.description(),
                extracted.amount()
        ));
    }

    private SuggestionResult fallbackToLLM(SuggestionInput transactionInput) {
        log.debug("No embedding found for the input, falling back to LLM.");
        var suggestion = classificationAgent.classifyTransaction(UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
        return new SuggestionResult(suggestion.category(), suggestion.subCategory(), suggestion.tags());
    }

    private String translateDate(SuggestionInput transactionInput) {
        if (transactionInput.date() != null) {
            return transactionInput.date().format(DateTimeFormatter.BASIC_ISO_DATE);
        }

        return "";
    }

}
