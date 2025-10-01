package com.jongsoft.finance.rest.statistic;

import static com.jongsoft.finance.rest.ApiConstants.TAG_REPORTS;

import com.jongsoft.finance.providers.SpendingInsightProvider;
import com.jongsoft.finance.providers.SpendingPatternProvider;
import com.jongsoft.finance.rest.model.SpendingInsightResponse;
import com.jongsoft.finance.rest.model.SpendingPatternResponse;
import com.jongsoft.finance.security.AuthenticationRoles;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.YearMonth;
import java.util.List;

@Tag(name = TAG_REPORTS)
@Controller("/api/statistics/spending")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class SpendingInsightResource {

    private final SpendingInsightProvider spendingInsightProvider;
    private final SpendingPatternProvider spendingPatternProvider;

    public SpendingInsightResource(
            SpendingInsightProvider spendingInsightProvider,
            SpendingPatternProvider spendingPatternProvider) {
        this.spendingInsightProvider = spendingInsightProvider;
        this.spendingPatternProvider = spendingPatternProvider;
    }

    @Get("/insights")
    @Operation(
            summary = "Get spending insights",
            description = "Get spending insights for a specific year and month",
            operationId = "getSpendingInsights")
    List<SpendingInsightResponse> getInsights(
            @QueryValue @Parameter(description = "The year", example = "2023") int year,
            @QueryValue @Parameter(description = "The month (1-12)", example = "1") @Min(1) @Max(12)
                    int month) {
        return spendingInsightProvider
                .lookup(YearMonth.of(year, month))
                .map(SpendingInsightResponse::new)
                .toJava();
    }

    @Get("/patterns")
    @Operation(
            summary = "Get spending patterns",
            description = "Get spending patterns for a specific year and month",
            operationId = "getSpendingPatterns")
    List<SpendingPatternResponse> getPatterns(
            @QueryValue @Parameter(description = "The year", example = "2023") int year,
            @QueryValue @Parameter(description = "The month (1-12)", example = "1") @Min(1) @Max(12)
                    int month) {
        return spendingPatternProvider
                .lookup(YearMonth.of(year, month))
                .map(SpendingPatternResponse::new)
                .toJava();
    }
}
