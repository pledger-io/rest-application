package com.jongsoft.finance.domain.insight;

/** Types of spending insights that can be detected. */
public enum InsightType {
  UNUSUAL_AMOUNT, // Transaction amount is unusually high or low for this category
  UNUSUAL_FREQUENCY, // Transaction frequency is unusual for this category
  UNUSUAL_MERCHANT, // Transaction with an unusual merchant for this category
  UNUSUAL_TIMING, // Transaction at an unusual time (day/hour) for this category
  POTENTIAL_DUPLICATE, // Transaction might be a duplicate of another transaction
  BUDGET_EXCEEDED, // Transaction causes a budget to be exceeded
  SPENDING_SPIKE, // Sudden spike in spending in a category
  UNUSUAL_LOCATION // Transaction in an unusual location
}
