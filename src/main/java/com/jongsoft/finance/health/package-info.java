/// # Health
/// > Represent monthly financial health snapshots.
///
/// ## Boundary
/// - `lookup-health-snapshot`
/// - `compute-health`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a component requests a snapshot for a month, the application shall look up the corresponding health snapshot.
///
/// ## Entities
/// - HealthSnapshot, HealthComponent
///
/// ## Out of scope
/// - Financial health calculation is not yet implemented.
package com.jongsoft.finance.health;
