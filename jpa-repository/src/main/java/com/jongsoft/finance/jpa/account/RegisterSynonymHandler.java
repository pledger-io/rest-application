package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RegisterSynonymCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class RegisterSynonymHandler implements CommandHandler<RegisterSynonymCommand> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    RegisterSynonymHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(RegisterSynonymCommand command) {
        log.info("[{}] - Processing register synonym event", command.accountId());

        var existingId = entityManager.from(AccountSynonymJpa.class)
                .fieldEq("synonym", command.synonym())
                .fieldEq("account.user.username", authenticationFacade.authenticated())
                .projectSingleValue(Long.class, "id");

        var account = entityManager.from(AccountJpa.class)
                .joinFetch("currency")
                .joinFetch("user")
                .fieldEq("id", command.accountId())
                .singleResult()
                .get();

        if (existingId.isPresent()) {
            entityManager.update(AccountSynonymJpa.class)
                    .set("account", account)
                    .fieldEq("id", existingId.get())
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
