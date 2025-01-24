package com.jongsoft.finance.domain;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;
import lombok.Getter;

import java.time.LocalDateTime;

public class FinTrack {

    @Getter
    private final Encoder hashingAlgorithm;

    public FinTrack(Encoder hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public UserAccount createUser(String username, String password) {
        return new UserAccount(username, password);
    }

    public void registerToken(String username, String token, Integer expiresIn) {
        RegisterTokenCommand.tokenRegistered(username, token, LocalDateTime.now().plusSeconds(expiresIn));
    }

}
