/// # Exporter
/// > Import and export financial data.
///
/// ## Boundary
/// - `create-configuration`
/// - `get-configurations`
/// - `create-job`
/// - `delete-job`
/// - `get-job`
/// - `find-jobs`
/// - `get-tasks`
/// - `complete-task`
/// - `export-user-account`
/// - `export-transactions`
/// - `import-user-account`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a user creates or requests import configurations, the application shall store or
/// return the configurations.
///
/// ### R2
/// - R2.1 — When a user creates, retrieves or deletes an import job, the application shall manage
/// the job.
///
/// ### R3
/// - R3.1 — When a user lists import jobs or requests job tasks, the application shall return
/// matching jobs or tasks.
///
/// ### R4
/// - R4.1 — When a user completes an import task, the application shall process the submitted task
/// data.
///
/// ### R5
/// - R5.1 — When a user requests an account or transaction export, the application shall produce
/// the requested export.
///
/// ### R6
/// - R6.1 — When a user submits an account profile for import, the application shall import the
/// profile.
///
/// ## Entities
/// - BatchImport, BatchImportConfig, ImporterConfiguration, ProcessConfiguration, UserTask,
/// AccountMapping
package com.jongsoft.finance.exporter;
