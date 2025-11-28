package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;

public interface CurrentUserProvider {

    /**
     * @return the current user or null if no user is logged in
     */
    UserAccount currentUser();
}
