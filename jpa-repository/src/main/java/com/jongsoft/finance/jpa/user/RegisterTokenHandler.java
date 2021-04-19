package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class RegisterTokenHandler implements CommandHandler<RegisterTokenCommand> {

    private final ReactiveEntityManager entityManager;

    public RegisterTokenHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RegisterTokenCommand command) {
        log.trace("[{}] - Registering new security token.", command.username());

        var userAccountJpa = entityManager.get(UserAccountJpa.class, Collections.Map("username", command.username()));

        var refreshJpa = AccountTokenJpa.builder()
                .user(userAccountJpa)
                .refreshToken(command.refreshToken())
                .expires(command.expireDate())
                .build();

        entityManager.persist(refreshJpa);
    }
}
