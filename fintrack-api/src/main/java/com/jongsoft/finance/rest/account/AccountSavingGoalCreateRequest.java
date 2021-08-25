package com.jongsoft.finance.rest.account;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
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
