package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.RevokeTokenCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Singleton
public class RevokeTokenHandler implements CommandHandler<RevokeTokenCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public RevokeTokenHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RevokeTokenCommand command) {
        log.info("[{}] - Revoking security token.", command.token());

        var hql = """
                update AccountTokenJpa
                set expires = :now 
                where refreshToken = :token
                    and expires > :now""";

        entityManager.update()
                .hql(hql)
                .set("token", command.token())
                .set("now", LocalDateTime.now())
                .execute();
    }

}
