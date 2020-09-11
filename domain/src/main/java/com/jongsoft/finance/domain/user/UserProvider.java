package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.lang.control.Optional;

public interface UserProvider extends DataProvider<UserAccount> {

    boolean available(String username);

    Optional<UserAccount> lookup(String username);

    default boolean supports(Class<UserAccount> supportingClass) {
        return UserAccount.class.equals(supportingClass);
    }
}
