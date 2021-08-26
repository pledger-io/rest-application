package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.DeleteTagCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeleteTagHandler implements CommandHandler<DeleteTagCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public DeleteTagHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(DeleteTagCommand command) {
        log.info("[{}] - Processing tag deletion event", command.tag());

        var hql = """
                update %s
                set archived = true
                where user.id = (select u.id from %s u where username = :username)
                    and name = :name""".formatted(TagJpa.class.getName(), UserAccountJpa.class.getName());

        entityManager.update()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", command.tag())
                .execute();
    }

}
