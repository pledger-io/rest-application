package com.jongsoft.finance.domain;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;

import java.time.LocalDateTime;

public class FinTrack {

    private FinTrack() {

    }

    public static UserAccount createUser(String username, String password) {
        return new UserAccount(username, password);
    }

    public static void registerToken(String username, String token, Integer expiresIn) {
        EventBus.getBus().send(new RegisterTokenCommand(
                username,
                token,
                LocalDateTime.now()
                        .plusSeconds(expiresIn)
        ));
    }

}
