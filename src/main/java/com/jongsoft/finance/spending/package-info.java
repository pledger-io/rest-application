/// # Spending
/// > Analyze spending and report insights and patterns.
///
/// ## Boundary
/// - `get-insights`
/// - `get-patterns`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When spending analysis is scheduled and enabled, the application shall run analysis
/// jobs to detect insights and patterns.
///
/// ### R2
/// - R2.1 — When a user requests insights or patterns for a month, the application shall return
/// matching results.
///
/// ### R3
/// - R3.1 — When the spending report is scheduled and enabled, the application shall send a report
/// for the previous month.
///
/// ## Entities
/// - AnalyzeJob, SpendingInsight, SpendingPattern
package com.jongsoft.finance.spending;
