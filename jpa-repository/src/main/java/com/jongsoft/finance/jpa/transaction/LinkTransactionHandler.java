package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.providers.DataProvider;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class LinkTransactionHandler implements CommandHandler<LinkTransactionCommand> {

    private final ReactiveEntityManager entityManager;
    private final List<DataProvider<? extends Classifier>> metadataProviders;

    @Inject
    public LinkTransactionHandler(
            ReactiveEntityManager entityManager,
            List<DataProvider<? extends Classifier>> metadataProviders) {
        this.entityManager = entityManager;
        this.metadataProviders = metadataProviders;
    }

    @Override
    @BusinessEventListener
    public void handle(LinkTransactionCommand command) {
        log.info("[{}] - Processing transaction relation change {}", command.id(), command.type());

        // remove any outdated relationships
        entityManager
                .getEntityManager()
                .createQuery(
                        "delete from TransactionMetaJpa t where t.journal.id = :id and t.relationType = :type")
                .setParameter("id", command.id())
                .setParameter("type", command.type().name())
                .executeUpdate();
        if (command.relationId() == null) {
            return;
        }

        var relatedEntity = metadataProviders.stream()
                .filter(provider ->
                        Objects.equals(provider.typeOf(), command.type().name()))
                .findFirst()
                .map(provider -> provider.lookup(command.relationId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow(() -> StatusException.badRequest(
                        "Could not locate " + command.type() + " with id " + command.relationId()));

        var journal = entityManager.getById(TransactionJournal.class, command.id());
        var entity = new TransactionMetaJpa(journal, command.type().name(), relatedEntity.getId());
        entityManager.persist(entity);
        // needed to update hibernate level-1 cache
        journal.getMetadata().add(entity);
    }
}
