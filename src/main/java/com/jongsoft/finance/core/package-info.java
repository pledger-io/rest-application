/// # Core
/// > Provide users, security and shared application services.
///
/// ## Boundary
/// - `create-user`
/// - `verify-two-factor`
/// - `get-jwt-signature`
/// - `get-profile`
/// - `patch-profile`
/// - `patch-two-factor`
/// - `generate-qr-code`
/// - `create-session`
/// - `list-sessions`
/// - `revoke-session`
/// - `create-currency`
/// - `get-currencies`
/// - `get-currency`
/// - `patch-currency`
/// - `update-currency`
/// - `upload-file`
/// - `download-file`
/// - `delete-file`
/// - `get-translations`
/// - `get-openid-configuration`
/// - `get-settings`
/// - `patch-setting`
///
/// ## Requirements
/// ### R1
/// - R1.1 — When a user registers, the application shall create a user account.
///
/// ### R2
/// - R2.1 — When a user requests or changes a profile, the application shall return or update its details.
///
/// ### R3
/// - R3.1 — When a user configures or verifies two-factor authentication, the application shall update the setting or verify the token.
///
/// ### R4
/// - R4.1 — When a user creates, lists or revokes a session, the application shall manage session tokens.
///
/// ### R5
/// - R5.1 — When a user creates, retrieves or updates a currency, the application shall manage currency data.
///
/// ### R6
/// - R6.1 — When a user uploads, downloads or deletes a file, the application shall perform the requested storage operation.
///
/// ### R7
/// - R7.1 — When a user requests translations, settings or OpenID configuration, the application shall return the requested information.
///
/// ### R8
/// - R8.1 — When a user changes a setting, the application shall update it.
///
/// ## Entities
/// - UserAccount, SessionToken, Currency, Setting, Role
package com.jongsoft.finance.core;
