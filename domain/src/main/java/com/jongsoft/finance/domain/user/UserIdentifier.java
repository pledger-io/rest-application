package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.Identifier;

/**
 * Represents a UserIdentifier, which is an implementation of an Identifier for uniquely identifying a user based on their email.
 * This record class extends the Identifier interface and encapsulates the user's email address as the identifying value.
 */
public record UserIdentifier(String email) implements Identifier {
}
