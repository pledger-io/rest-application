package com.jongsoft.finance.spending.types;

/** Types of spending patterns that can be detected. */
public enum PatternType {
    RECURRING_MONTHLY, // Regular monthly payments (e.g., rent, subscriptions)
    RECURRING_WEEKLY, // Regular weekly payments (e.g., groceries)
    SEASONAL, // Seasonal spending (e.g., holiday shopping, summer vacations)
    INCREASING_TREND, // Gradually increasing spending in a category
    DECREASING_TREND // Gradually decreasing spending in a category
}
