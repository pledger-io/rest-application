/// # Suggestion
/// > Manage transaction classification rules and suggestions.
///
/// ## Boundary
/// - `create-rule-group`
/// - `update-rule-group`
/// - `delete-rule-group`
/// - `fetch-rule-groups`
/// - `create-rule`
/// - `update-rule`
/// - `patch-rule`
/// - `delete-rule`
/// - `fetch-rule`
/// - `fetch-rules-by-group`
/// - `suggest-classifications`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a user creates, changes or deletes a rule group, the application shall manage the
/// group.
///
/// ### R2
/// - R2.1 — When a user requests rule groups, the application shall return them.
///
/// ### R3
/// - R3.1 — When a user creates, changes, reorders or deletes a rule, the application shall manage
/// the rule in its group.
///
/// ### R4
/// - R4.1 — When a user requests a rule or rules in a group, the application shall return matching
/// rules.
///
/// ### R5
/// - R5.1 — When a user requests transaction classifications, the application shall return
/// suggestions using the configured rule or AI engine.
///
/// ## Entities
/// - TransactionRuleGroup, TransactionRule, SuggestionInput, SuggestionResult
package com.jongsoft.finance.suggestion;
