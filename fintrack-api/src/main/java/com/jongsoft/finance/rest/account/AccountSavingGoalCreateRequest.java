package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Serdeable.Deserializable
public record AccountSavingGoalCreateRequest(
    @NotBlank String name, @NotNull @Positive BigDecimal goal, @NotNull LocalDate targetDate) {}
