package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RegisterAccountIconCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegisterAccountIconHandler implements CommandHandler<RegisterAccountIconCommand> {

    private final ReactiveEntityManager entityManager;

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
