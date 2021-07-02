package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangeMultiFactorCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeMultiFactorHandler implements CommandHandler<ChangeMultiFactorCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(ChangeMultiFactorCommand command) {
        log.info("[{}] - Updating multi factor setting", command.username());

        entityManager.update()
                .hql("""
                        update UserAccountJpa
                        set twoFactorEnabled = :enabled
                        where username = :username""")
                .set("username", command.username())
                .set("enabled", command.enabled())
                .execute();
    }

}
