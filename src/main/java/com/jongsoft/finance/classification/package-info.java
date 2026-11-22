/// # Classification
/// > Manage categories for financial activity.
///
/// ## Boundary
/// - `create-category`
/// - `update-category`
/// - `delete-category`
/// - `get-category`
/// - `find-categories`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a user submits a category, the application shall create it.
///
/// ### R2
/// - R2.1 — When a user changes or deletes a category, the application shall rename or delete it.
///
/// ### R3
/// - R3.1 — When a user requests or searches categories, the application shall return matching
/// categories.
///
/// ## Entities
/// - Category
package com.jongsoft.finance.classification;
