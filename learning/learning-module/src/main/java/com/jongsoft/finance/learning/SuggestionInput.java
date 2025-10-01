package com.jongsoft.finance.learning;

import java.time.LocalDate;

public record SuggestionInput(
        LocalDate date, String description, String fromAccount, String toAccount, double amount) {}
