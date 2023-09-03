package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.TagTransactionCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Singleton
public class TagTransactionHandler implements CommandHandler<TagTransactionCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public TagTransactionHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(TagTransactionCommand command) {
        log.info("[{}] - Processing transaction tagging event", command.id());

        var transaction = entityManager.get(TransactionJournal.class, Collections.Map("id", command.id()));
        transaction.getTags().clear();

        command.tags()
                .map(this::tag)
                .filter(Objects::nonNull)
                .forEach(tag -> transaction.getTags().add(tag));

        entityManager.persist(transaction);
    }

    private TagJpa tag(String name) {
        var hql = """
                select t from TagJpa t
                where t.name = :name and t.user.username = :username""";

        return entityManager.<TagJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrSupply(() -> null);
    }

}
