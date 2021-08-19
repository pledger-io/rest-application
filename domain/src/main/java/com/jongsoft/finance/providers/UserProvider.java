package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.control.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserProvider extends DataProvider<UserAccount> {

    boolean available(String username);

    Optional<UserAccount> lookup(String username);

    Mono<UserAccount> refreshToken(String refreshToken);

    Flux<SessionToken> tokens(String username);

    default boolean supports(Class<UserAccount> supportingClass) {
        return UserAccount.class.equals(supportingClass);
    }
}
