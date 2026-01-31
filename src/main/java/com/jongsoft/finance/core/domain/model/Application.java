package com.jongsoft.finance.core.domain.model;

import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.commands.RegisterTokenCommand;

import java.time.LocalDateTime;

public class Application {

    public static void authenticateUser(String username) {
        InternalAuthenticationEvent.authenticate(username);
    }

    public static void registerUserToken(
            String username, String refreshToken, LocalDateTime expireDate) {
        RegisterTokenCommand.tokenRegistered(username, refreshToken, expireDate);
    }
}
