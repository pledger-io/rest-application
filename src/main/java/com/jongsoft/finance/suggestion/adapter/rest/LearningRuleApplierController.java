package com.jongsoft.finance.suggestion.adapter.rest;

import com.jongsoft.finance.rest.LearningRuleApplierApi;
import com.jongsoft.finance.rest.model.SuggestClassifications200Response;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;

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
}
