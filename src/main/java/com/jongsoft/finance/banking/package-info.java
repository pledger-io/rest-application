/// # Banking
/// > Manage accounts, transactions, savings and recurring payments.
///
/// ## Boundary
/// - `create-account`, `update-account`, `delete-account`, `get-account`, `find-accounts`
/// - `get-account-types`, `get-top-accounts-by-spending`
/// - `create-saving-goal`, `update-saving-goal`, `complete-saving-goal`, `find-saving-goals`,
/// `reserve-saving-goal`
/// - `reconcile-account`, `find-reconciliations`
/// - `create-transaction`, `update-transaction`, `delete-transaction`, `split-transaction`,
/// `get-transaction`, `find-transactions`
/// - `create-tag`, `delete-tag`, `find-tags`
/// - `create-schedule`, `update-schedule`, `delete-schedule`, `get-schedule`, `find-schedules`
/// - `compute-balance`, `compute-dated-balances`, `compute-partitioned-balances`
/// - `extract-transaction`
///
/// ## Requirements
/// ### R1: Manage accounts
/// - R1.1 — When a user submits account details, the application shall create an account.
/// - R1.2 — When a user requests an account or searches by name or type, the application shall
/// return matching accounts.
/// - R1.3 — When a user changes or removes an account, the application shall update or terminate
/// it.
/// - R1.4 — When a user requests account types, the application shall return the available types.
///
/// ### R2: Manage savings
/// - R2.1 — When a user creates, changes or completes a saving goal, the application shall update
/// the account's goals.
/// - R2.2 — When a user requests an account's saving goals, the application shall return them.
/// - R2.3 — When a user reserves an amount for a saving goal, the application shall register the
/// payment.
///
/// ### R3: Reconcile accounts
/// - R3.1 — When a user submits balances for reconciliation, the application shall reconcile the
/// account.
/// - R3.2 — When a user requests reconciliations for an account, the application shall return them.
///
/// ### R4: Manage transactions
/// - R4.1 — When a user submits transaction details, the application shall create a transaction.
/// - R4.2 — When a user requests a transaction or searches using supported filters, the application
/// shall return matching transactions.
/// - R4.3 — When a user changes or deletes a transaction, the application shall update or delete
/// it.
/// - R4.4 — When a user submits transaction splits, the application shall split the transaction.
/// - R4.5 — When a user supplies a category, expense, contract or tags for a transaction, the
/// application shall associate them with it.
///
/// ### R5: Manage tags
/// - R5.1 — When a user creates or deletes a tag, the application shall create or delete it.
/// - R5.2 — When a user searches for tags by name, the application shall return matching tags.
///
/// ### R6: Schedule transactions
/// - R6.1 — When a user creates, changes or terminates a schedule, the application shall update the
/// schedule.
/// - R6.2 — When a user requests a schedule or searches schedules, the application shall return
/// matching schedules.
/// - R6.3 — When a scheduled transaction is due, the application shall create a transaction and
/// reschedule it.
///
/// ### R7: Review balances and spending
/// - R7.1 — When a user requests balances, the application shall calculate balances for the
/// supplied filters, dates or partitions.
/// - R7.2 — When a user requests top accounts by spending for a period, the application shall
/// return ranked accounts.
///
/// ### R8: Extract a transaction
/// - R8.1 — When a user submits text for extraction, the application shall return extracted
/// transaction details if extraction succeeds.
/// - R8.2 — If transaction extraction fails, the application shall reject the request.
///
/// ## Entities
/// - Account, Transaction, TransactionSchedule, SavingGoal, Tag
///
/// ## Out of scope
/// - Transaction filtering by tag is not implemented.
package com.jongsoft.finance.banking;
