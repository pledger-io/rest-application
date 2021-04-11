package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

public interface UserProvider extends DataProvider<UserAccount> {

    boolean available(String username);

    Optional<UserAccount> lookup(String username);

    Maybe<UserAccount> refreshToken(String refreshToken);

    Flowable<SessionToken> tokens(String username);

    default boolean supports(Class<UserAccount> supportingClass) {
        return UserAccount.class.equals(supportingClass);
    }
}
