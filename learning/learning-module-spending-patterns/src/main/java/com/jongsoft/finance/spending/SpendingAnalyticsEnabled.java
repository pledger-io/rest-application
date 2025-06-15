package com.jongsoft.finance.spending;

import io.micronaut.context.annotation.Requires;

@Requires(notEnv = "no-analytics")
public @interface SpendingAnalyticsEnabled {
}
