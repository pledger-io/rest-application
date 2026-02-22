package com.jongsoft.finance.core.adapter.api;

import com.jongsoft.finance.core.domain.model.UserAccount;

public interface CurrentUserProvider {

    /**
     * @return the current user or null if no user is logged in
     */
    UserAccount currentUser();
}
