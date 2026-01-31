package com.jongsoft.finance.suggestion.domain.model;

import java.util.List;

public record SuggestionResult(String budget, String category, List<String> tags) {}
