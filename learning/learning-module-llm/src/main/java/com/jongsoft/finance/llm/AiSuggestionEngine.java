package com.jongsoft.finance.llm;

import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Primary
@Singleton
@AiEnabled
class AiSuggestionEngine implements SuggestionEngine {

    private final ClassificationAgent classificationAgent;

    public AiSuggestionEngine(ClassificationAgent classificationAgent) {
        this.classificationAgent = classificationAgent;
    }

    @Override
    public SuggestionResult makeSuggestions(SuggestionInput transactionInput) {
        return new SuggestionResult(
                suggestBudget(transactionInput),
                suggestCategory(transactionInput),
                suggestTags(transactionInput)
        );
    }

    private List<String> suggestTags(SuggestionInput transactionInput) {
        return classificationAgent.determineTags(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
    }

    private String suggestCategory(SuggestionInput transactionInput) {
        return classificationAgent.determineCategory(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
    }

    private String suggestBudget(SuggestionInput transactionInput) {
        return classificationAgent.determineBudget(
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
