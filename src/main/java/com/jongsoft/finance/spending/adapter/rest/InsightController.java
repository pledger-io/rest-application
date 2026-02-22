package com.jongsoft.finance.spending.adapter.rest;

import com.jongsoft.finance.rest.InsightApi;
import com.jongsoft.finance.rest.model.DetectedInsightResponse;
import com.jongsoft.finance.rest.model.DetectedPatternResponse;
import com.jongsoft.finance.spending.adapter.api.SpendingInsightProvider;
import com.jongsoft.finance.spending.adapter.api.SpendingPatternProvider;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;

@Controller
public class InsightController implements InsightApi {

    private final Logger logger;
    private final SpendingInsightProvider spendingInsightProvider;
    private final SpendingPatternProvider spendingPatternProvider;

    public InsightController(
            SpendingInsightProvider spendingInsightProvider,
            SpendingPatternProvider spendingPatternProvider) {
        this.spendingInsightProvider = spendingInsightProvider;
        this.spendingPatternProvider = spendingPatternProvider;
        this.logger = org.slf4j.LoggerFactory.getLogger(InsightController.class);
    }

    @Override
    public List<DetectedInsightResponse> getInsightsByFilters(Integer year, Integer month) {
        logger.info("Fetching insights by filters.");

        return spendingInsightProvider
                .lookup(YearMonth.of(year, month))
                .map(InsightMapper::toInsightResponse)
                .toJava();
    }

    @Override
    public List<DetectedPatternResponse> getPatternsByFilters(Integer year, Integer month) {
        logger.info("Fetching patterns by filters.");

        return spendingPatternProvider
                .lookup(YearMonth.of(year, month))
                .map(InsightMapper::toPatternResponse)
                .toJava();
    }
}
