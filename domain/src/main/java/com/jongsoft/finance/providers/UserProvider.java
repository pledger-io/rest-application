package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface UserProvider extends DataProvider<UserAccount> {

  Sequence<UserAccount> lookup();

  boolean available(UserIdentifier username);

  Optional<UserAccount> lookup(UserIdentifier username);

  Optional<UserAccount> refreshToken(String refreshToken);

  Sequence<SessionToken> tokens(UserIdentifier username);

  default boolean supports(Class<?> supportingClass) {
    return UserAccount.class.equals(supportingClass);
  }
}
