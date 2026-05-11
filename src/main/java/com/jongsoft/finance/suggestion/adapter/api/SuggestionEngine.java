package com.jongsoft.finance.suggestion.adapter.api;

import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface SuggestionEngine {

    SuggestionResult makeSuggestions(SuggestionInput transactionInput);

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rule {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface AI {}
}
