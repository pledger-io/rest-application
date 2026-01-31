package com.jongsoft.finance.spending.domain.jpa.filter;

import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.adapter.api.SpendingInsightProvider;
import com.jongsoft.finance.spending.adapter.api.SpendingPatternProvider;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
class SpendingFilterFactory {

    @Bean
    FilterProvider<SpendingInsightProvider.FilterCommand> spendingFilterProvider() {
        return SpendingInsightFilterCommand::new;
    }

    @Bean
    FilterProvider<SpendingPatternProvider.FilterCommand> spendingPatternFilterProvider() {
        return SpendingPatternFilterCommand::new;
    }
}
