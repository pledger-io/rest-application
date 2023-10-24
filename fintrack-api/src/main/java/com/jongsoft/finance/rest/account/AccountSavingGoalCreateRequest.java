package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Serdeable.Deserializable
public class AccountSavingGoalCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal goal;

    @NotNull
    private LocalDate targetDate;

}
