package com.jongsoft.finance.llm;

import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.stores.ClassificationEmbeddingStore;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public AiSuggestionEngine(ClassificationEmbeddingStore embeddingStore, ClassificationAgent classificationAgent) {
        this.embeddingStore = embeddingStore;
        this.classificationAgent = classificationAgent;
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

    private SuggestionResult fallbackToLLM(SuggestionInput transactionInput) {
        log.debug("No embedding found for the input, falling back to LLM.");
        return new SuggestionResult(suggestBudget(transactionInput), suggestCategory(transactionInput), suggestTags(transactionInput));
    }

    private List<String> suggestTags(SuggestionInput transactionInput) {
        log.trace("Classifying the tags.");
        return classificationAgent.determineTags(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
    }

    private String suggestCategory(SuggestionInput transactionInput) {
        log.trace("Classifying the category.");
        return classificationAgent.determineSubCategory(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
    }

    private String suggestBudget(SuggestionInput transactionInput) {
        log.trace("Classifying the budget.");
        return classificationAgent.determineCategory(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
    }

    private String translateDate(SuggestionInput transactionInput) {
        if (transactionInput.date() != null) {
            return transactionInput.date().format(DateTimeFormatter.BASIC_ISO_DATE);
        }

        return "";
    }

}
