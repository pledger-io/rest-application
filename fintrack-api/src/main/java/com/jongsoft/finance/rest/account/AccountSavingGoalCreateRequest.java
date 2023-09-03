package com.jongsoft.finance.rest.account;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Introspected
public class AccountSavingGoalCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal goal;

    @NotNull
    private LocalDate targetDate;

}
