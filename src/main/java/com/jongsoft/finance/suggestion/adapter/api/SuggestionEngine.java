package com.jongsoft.finance.suggestion.adapter.api;

import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;

public interface SuggestionEngine {

    SuggestionResult makeSuggestions(SuggestionInput transactionInput);
}
