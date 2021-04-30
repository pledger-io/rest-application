package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RegisterSynonymCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class RegisterSynonymHandler implements CommandHandler<RegisterSynonymCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public RegisterSynonymHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }


    @Override
    @BusinessEventListener
    public void handle(RegisterSynonymCommand command) {
        log.info("[{}] - Processing register synonym event", command.accountId());

        var hql = """
                select id from AccountSynonymJpa where
                    synonym = :synonym
                    and account.user.username = :username""";

        var existingId = entityManager.blocking()
                .hql(hql)
                .set("synonym", command.synonym())
                .set("username", authenticationFacade.authenticated())
                .maybe();

        var account = entityManager.get(
                AccountJpa.class,
                Collections.Map("id", command.accountId()));

        if (existingId.isPresent()) {
            var updateHql = """
                    update AccountSynonymJpa 
                    set account = :account 
                    where id = :id""";

            entityManager.update()
                    .hql(updateHql)
                    .set("account", account)
                    .set("id", existingId.get())
                    .execute();
        } else {
            var entity = AccountSynonymJpa.builder()
                    .account(account)
                    .synonym(command.synonym())
                    .build();

            entityManager.persist(entity);
        }
    }

}
