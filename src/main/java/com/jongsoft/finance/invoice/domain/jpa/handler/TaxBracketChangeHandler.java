package com.jongsoft.finance.invoice.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.domain.commands.CreateTaxBracketCommand;
import com.jongsoft.finance.invoice.domain.commands.DeleteTaxBracketCommand;
import com.jongsoft.finance.invoice.domain.commands.UpdateTaxBracketCommand;
import com.jongsoft.finance.invoice.domain.jpa.entity.TaxBracketJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class TaxBracketChangeHandler {
    private final Logger log = LoggerFactory.getLogger(TaxBracketChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    TaxBracketChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateTaxBracket(CreateTaxBracketCommand command) {
        log.info("[{}] - Processing tax bracket create event", command.name());

        var toCreate =
                TaxBracketJpa.of(command.name(), command.rate(), entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateTaxBracket(UpdateTaxBracketCommand command) {
        log.info("[{}] - Processing tax bracket update event", command.id());

        entityManager
                .update(TaxBracketJpa.class)
                .set("name", command.name())
                .set("rate", command.rate())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleDeleteTaxBracket(DeleteTaxBracketCommand command) {
        log.info("[{}] - Processing tax bracket delete event", command.id());

        entityManager
                .from(TaxBracketJpa.class)
                .fieldEq("id", command.id())
                .singleResult()
                .ifPresent(entity -> entityManager.getEntityManager().remove(entity));
    }
}
