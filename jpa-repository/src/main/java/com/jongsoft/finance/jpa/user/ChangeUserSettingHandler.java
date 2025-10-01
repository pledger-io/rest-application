package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangeUserSettingCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.Currency;

@Slf4j
@Singleton
@Transactional
public class ChangeUserSettingHandler implements CommandHandler<ChangeUserSettingCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeUserSettingHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeUserSettingCommand command) {
        log.info("[{}] - Updating user setting {}", command.username(), command.type());

        var query =
                entityManager
                        .update(UserAccountJpa.class)
                        .fieldEq("username", command.username().email());

        switch (command.type()) {
            case THEME -> query.set("theme", command.value());
            case CURRENCY -> query.set("currency", Currency.getInstance(command.value()));
            default -> {}
        }
        query.execute();
    }
}
