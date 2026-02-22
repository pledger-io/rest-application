package com.jongsoft.finance.core.adapter.api;

import com.jongsoft.finance.core.domain.model.SessionToken;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface UserProvider {

    Sequence<UserAccount> lookup();

    Optional<UserAccount> lookup(long id);

    boolean available(UserIdentifier username);

    Optional<UserAccount> lookup(UserIdentifier username);

    Optional<UserAccount> refreshToken(String refreshToken);

    Sequence<SessionToken> tokens(UserIdentifier username);
}
