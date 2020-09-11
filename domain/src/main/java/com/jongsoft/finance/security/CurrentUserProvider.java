package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;

public interface CurrentUserProvider {

    UserAccount currentUser();

}
