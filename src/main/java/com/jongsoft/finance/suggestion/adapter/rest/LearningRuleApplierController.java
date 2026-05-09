package com.jongsoft.finance.suggestion.adapter.rest;

import com.jongsoft.finance.core.adapter.api.SettingProvider;
import com.jongsoft.finance.rest.LearningRuleApplierApi;
import com.jongsoft.finance.rest.model.SuggestClassifications200Response;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine.AI;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine.Rule;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

@Controller
class LearningRuleApplierController implements LearningRuleApplierApi {

    private final Logger logger = LoggerFactory.getLogger(LearningRuleApplierController.class);

    private final SettingProvider settingProvider;
    private final SuggestionEngine ruleSuggestionEngine;
    private final SuggestionEngine aiSuggestionEngine;

    public LearningRuleApplierController(
            SettingProvider settingProvider,
            @Rule SuggestionEngine ruleSuggestionEngine,
            @AI SuggestionEngine aiSuggestionEngine) {
        this.settingProvider = settingProvider;
        this.ruleSuggestionEngine = ruleSuggestionEngine;
        this.aiSuggestionEngine = aiSuggestionEngine;
    }

    @Override
    public SuggestClassifications200Response suggestClassifications(
            Double amount, String description, String source, String destination) {
        logger.info("Suggesting classifications for transaction: {}", description);
        SuggestionInput convertedRequest = new SuggestionInput(
                LocalDate.now(),
                description,
                source,
                destination,
                Optional.ofNullable(amount).orElse(0.0));

        String classificationMode = settingProvider.getClassificationMode();
        if ("ai".equals(classificationMode)) {
            logger.debug("Using AI classification mode for transaction.");
            return createResponse(aiSuggestionEngine.makeSuggestions(convertedRequest));
        }

        return createResponse(ruleSuggestionEngine.makeSuggestions(convertedRequest));
    }

    private SuggestClassifications200Response createResponse(SuggestionResult result) {
        var response = new SuggestClassifications200Response();
        response.setBudget(result.budget());
        response.setCategory(result.category());
        response.setTags(result.tags());
        return response;
    }
}
