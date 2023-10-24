package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface UserProvider extends DataProvider<UserAccount> {

    boolean available(String username);

    Optional<UserAccount> lookup(String username);

    Optional<UserAccount> refreshToken(String refreshToken);

    Sequence<SessionToken> tokens(String username);

    default boolean supports(Class<UserAccount> supportingClass) {
        return UserAccount.class.equals(supportingClass);
    }
}
