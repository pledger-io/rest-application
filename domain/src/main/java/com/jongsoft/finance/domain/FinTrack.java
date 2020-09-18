package com.jongsoft.finance.domain;

import com.jongsoft.finance.domain.user.UserAccount;

public class FinTrack {

    private FinTrack() {

    }

    public static UserAccount createUser(String username, String password) {
        return new UserAccount(username, password);
    }

}
