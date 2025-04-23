package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
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

        var updateQuery = entityManager.update(TransactionJournal.class)
                .fieldEq("id", command.id());

        switch (command.type()) {
            case CATEGORY -> updateQuery.set("category", fetchRelation(command.type(), command.relation()));
            case CONTRACT -> updateQuery.set("contract", fetchRelation(command.type(), command.relation()));
            case EXPENSE -> updateQuery.set("budget", fetchRelation(command.type(), command.relation()));
            case IMPORT -> updateQuery.set("batchImport", fetchRelation(command.type(), command.relation()));
        }
        updateQuery.execute();
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
        if (label == null) {
            return null;
        }
        return entityManager.from(CategoryJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("label", label)
                .singleResult()
                .getOrThrow(() -> new IllegalArgumentException("Category not found"));
    }

    private ExpenseJpa expense(String name) {
        if (name == null) {
            return null;
        }
        return entityManager.from(ExpenseJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .getOrThrow(() -> new IllegalArgumentException("Budget not found"));
    }

    private ContractJpa contract(String name) {
        if (name == null) {
            return null;
        }
        return entityManager.from(ContractJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .getOrThrow(() -> new IllegalArgumentException("Contract not found"));
    }

    private ImportJpa job(String slug) {
        return entityManager.from(ImportJpa.class)
                .fieldEq("slug", slug)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .getOrThrow(() -> new IllegalArgumentException("Job not found"));
    }

}
