package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class LinkTransactionHandler implements CommandHandler<LinkTransactionCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public LinkTransactionHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(LinkTransactionCommand command) {
        log.info("[{}] - Processing transaction relation change {}", command.id(), command.type());

        var update = switch (command.type()) {
            case CATEGORY -> " category = :relation";
            case CONTRACT -> " contract = :relation";
            case EXPENSE -> " budget = :relation";
            case IMPORT -> " batchImport = :relation";
        };

        var hql = "update TransactionJournal set"
                + update
                + " where id = :id";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("relation", fetchRelation(command.type(), command.relation()))
                .execute();
    }

    private EntityJpa fetchRelation(LinkTransactionCommand.LinkType type, String relation) {
        return switch (type) {
            case CATEGORY -> category(relation);
            case CONTRACT -> contract(relation);
            case EXPENSE -> expense(relation);
            case IMPORT -> job(relation);
        };
    }


    private CategoryJpa category(String label) {
        var hql = """
                select c from CategoryJpa c 
                where c.label = :label and c.user.username = :username""";
        return entityManager.<CategoryJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("label", label)
                .maybe()
                .getOrThrow(() -> new IllegalArgumentException("Category not found"));
    }

    private ExpenseJpa expense(String name) {
        var hql = """
                select e from ExpenseJpa e
                where e.name = :name and e.user.username = :username""";

        return entityManager.<ExpenseJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrThrow(() -> new IllegalArgumentException("Budget not found"));
    }

    private ContractJpa contract(String name) {
        var hql = """
                select e from ContractJpa e
                where e.name = :name and e.user.username = :username and e.archived = false""";

        return entityManager.<ContractJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrThrow(() -> new IllegalArgumentException("Contract not found"));
    }

    private ImportJpa job(String slug) {
        var hql = """
                select e from ImportJpa e
                where e.slug = :slug and e.user.username = :username""";

        return entityManager.<ImportJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("slug", slug)
                .maybe()
                .getOrThrow(() -> new IllegalArgumentException("Job not found"));
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
                .getOrThrow(() -> new IllegalArgumentException("tag not found"));
    }
}
