package com.jongsoft.finance.suggestion.domain.model;

import java.time.LocalDate;

public record SuggestionInput(
        LocalDate date, String description, String fromAccount, String toAccount, double amount) {}
