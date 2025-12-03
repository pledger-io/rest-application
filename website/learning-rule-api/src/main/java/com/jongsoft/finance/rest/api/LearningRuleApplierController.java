package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.learning.TransactionResult;
import com.jongsoft.finance.rest.LearningRuleApplierApi;
import com.jongsoft.finance.rest.model.rule.*;

import io.micronaut.http.annotation.Controller;

import java.time.LocalDate;
import java.util.Optional;

@Controller
class LearningRuleApplierController implements LearningRuleApplierApi {

    private final SuggestionEngine suggestionEngine;

    public LearningRuleApplierController(SuggestionEngine suggestionEngine) {
        this.suggestionEngine = suggestionEngine;
    }

    @Override
    public SuggestClassifications200Response suggestClassifications(
            Double amount, String description, String source, String destination) {
        SuggestionInput convertedRequest = new SuggestionInput(
                LocalDate.now(),
                description,
                source,
                destination,
                Optional.ofNullable(amount).orElse(0.0));

        SuggestionResult suggested = suggestionEngine.makeSuggestions(convertedRequest);

        var response = new SuggestClassifications200Response();
        response.setBudget(suggested.budget());
        response.setCategory(suggested.category());
        response.setTags(suggested.tags());
        return response;
    }

    @Override
    public ExtractedResponse extractTransaction(
            ExtractTransactionRequest extractTransactionRequest) {

        TransactionResult extracted = suggestionEngine
                .extractTransaction(extractTransactionRequest.getText())
                .orElseThrow(() ->
                        StatusException.badRequest("Unable to extract transaction from text."));

        ExtractedResponse response = new ExtractedResponse();
        response.setDate(extracted.date());
        response.setAmount(extracted.amount());
        response.setDescription(extracted.description());
        if (extracted.from() != null) {
            response.setFrom(new ExtractedResponseFrom(
                    extracted.from().id(), extracted.from().name()));
        }
        if (extracted.to() != null) {
            response.setTo(new ExtractedResponseFrom(
                    extracted.to().id(), extracted.to().name()));
        }
        response.setType(ExtractedResponseType.valueOf(extracted.type().name()));
        return response;
    }
}
