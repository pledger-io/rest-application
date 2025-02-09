package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangePasswordCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ChangePasswordHandler implements CommandHandler<ChangePasswordCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangePasswordHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangePasswordCommand command) {
        log.info("[{}] - Updating password for user", command.username());

        entityManager.update(UserAccountJpa.class)
                .set("password", command.password())
                .fieldEq("username", command.username().email())
                .execute();
    }

}
