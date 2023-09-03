package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangeUserSettingCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Currency;

@Slf4j
@Singleton
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

        var query = entityManager.update()
                .set("username", command.username());

        var hql = "update UserAccountJpa set ";
        switch (command.type()) {
            case THEME -> {
                hql += " theme = :theme";
                query.set("theme", command.value());
            }
            case CURRENCY -> {
                hql += " currency = :currency";
                query.set("currency", Currency.getInstance(command.value()));
            }
            default -> {

            }
        }
        hql += " where username = :username";

        query.hql(hql)
                .execute();
    }
}
