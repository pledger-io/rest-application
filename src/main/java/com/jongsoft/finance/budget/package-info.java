/// # Budget
/// > Manage budgets and expenses.
///
/// ## Boundary
/// - `create-initial-budget`
/// - `update-current-budget`
/// - `update-expense`
/// - `compute-budget-expense-balance`
/// - `find-budget`
/// - `find-expenses`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a user submits an initial budget, the application shall create a budget for the requested period.
///
/// ### R2
/// - R2.1 — When a user changes a budget or expense, the application shall update the budget or create or update the expense.
///
/// ### R3
/// - R3.1 — When a user requests a budget, expenses or expense balances, the application shall return the requested data.
///
/// ## Entities
/// - Budget, ComputedExpense
package com.jongsoft.finance.budget;
