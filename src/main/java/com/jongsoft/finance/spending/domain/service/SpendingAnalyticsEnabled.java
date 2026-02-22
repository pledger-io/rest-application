package com.jongsoft.finance.spending.domain.service;

import io.micronaut.context.annotation.Requires;

@Requires(notEnv = "no-analytics")
public @interface SpendingAnalyticsEnabled {}
