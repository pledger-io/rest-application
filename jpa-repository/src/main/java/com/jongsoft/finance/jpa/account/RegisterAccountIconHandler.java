package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RegisterAccountIconCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class RegisterAccountIconHandler implements CommandHandler<RegisterAccountIconCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    RegisterAccountIconHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RegisterAccountIconCommand command) {
        log.info("[{}] - Processing icon registration event", command.id());

        var hql = """
                update AccountJpa
                set imageFileToken = :fileCode
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("fileCode", command.fileCode())
                .set("id", command.id())
                .execute();
    }

}
