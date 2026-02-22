package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.*;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;
import com.jongsoft.finance.core.domain.jpa.query.expression.FieldEquation;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.control.Optional;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Singleton
@Transactional
class TransactionChangeHandler implements TransactionCreationHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(TransactionChangeHandler.class);

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;
    private final List<LinkableProvider<? extends Classifier>> linkableProviders;

    TransactionChangeHandler(
            ReactiveEntityManager entityManager,
            AuthenticationFacade authenticationFacade,
            List<LinkableProvider<? extends Classifier>> linkableProviders) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.linkableProviders = linkableProviders;
    }

    @Override
    public long handleCreatedEvent(CreateTransactionCommand command) {
        log.info("[{}] - Processing transaction create event", command.description());

        var transactionJournal = new TransactionJournal(
                command.date(),
                null,
                null,
                command.description(),
                command.type(),
                command.failureCode(),
                entityManager.currentUser(),
                entityManager
                        .from(CurrencyJpa.class)
                        .fieldEq("code", command.currency())
                        .singleResult()
                        .get());

        entityManager.persist(transactionJournal);

        var toPart = new TransactionJpa(
                entityManager.getById(AccountJpa.class, command.toAccount()),
                transactionJournal,
                command.amount().abs(),
                command.description());
        var fromPart = new TransactionJpa(
                entityManager.getById(AccountJpa.class, command.fromAccount()),
                transactionJournal,
                command.amount().abs().multiply(BigDecimal.valueOf(-1)),
                command.description());
        entityManager.persist(toPart);
        entityManager.persist(fromPart);

        // Update hibernate cache
        transactionJournal.getTransactions().add(toPart);
        transactionJournal.getTransactions().add(fromPart);

        TransactionCreated.transactionCreated(transactionJournal.getId());

        return transactionJournal.getId();
    }

    @EventListener
    public void handleCreate(CreateTransactionCommand command) {
        handleCreatedEvent(command);
    }

    @EventListener
    public void handleChangePart(ChangeTransactionPartAccount command) {
        log.info("[{}] - Processing transaction account change", command.id());

        entityManager
                .update(TransactionJpa.class)
                .set("account.id", command.accountId())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleDatesChanged(ChangeTransactionDatesCommand command) {
        log.info("[{}] - Processing transaction book event", command.id());

        entityManager
                .update(TransactionJournal.class)
                .set("bookDate", command.bookingDate())
                .set("date", command.date())
                .set("interestDate", command.interestDate())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleAmoundChange(ChangeTransactionAmountCommand command) {
        log.info("[{}] - Processing transaction amount change event", command.id());

        entityManager
                .update(TransactionJpa.class)
                .set(
                        "amount",
                        Expressions.caseWhen(
                                Expressions.fieldCondition(null, "amount", FieldEquation.GTE, 0),
                                Expressions.value(command.amount()),
                                Expressions.value(command.amount().negate())))
                .fieldEq("journal.id", command.id())
                .execute();

        entityManager
                .update(TransactionJournal.class)
                .set(
                        "currency",
                        entityManager
                                .from(CurrencyJpa.class)
                                .fieldEq("code", command.currency())
                                .singleResult()
                                .get())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleLinkable(LinkTransactionCommand command) {
        log.info("[{}] - Processing transaction relation change {}", command.id(), command.type());

        // remove any outdated relationships
        entityManager
                .getEntityManager()
                .createQuery(
                        "delete from TransactionMetaJpa t where t.journal.id = :id and t.relationType = :type")
                .setParameter("id", command.id())
                .setParameter("type", command.type().name())
                .executeUpdate();

        var journal = entityManager.getById(TransactionJournal.class, command.id());
        if (command.relationId() == null) {
            journal.getMetadata()
                    .removeIf(m -> m.getRelationType().equals(command.type().name()));
            return;
        }

        var relatedEntity = linkableProviders.stream()
                .filter(provider ->
                        Objects.equals(provider.typeOf(), command.type().name()))
                .findFirst()
                .map(provider -> provider.lookup(command.relationId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow(() -> StatusException.badRequest(
                        "Could not locate " + command.type() + " with id " + command.relationId()));

        var entity = new TransactionMetaJpa(journal, command.type().name(), relatedEntity.getId());
        entityManager.persist(entity);
        // needed to update hibernate level-1 cache
        journal.getMetadata().add(entity);
    }

    @EventListener
    public void handleFailure(RegisterFailureCommand command) {
        log.info("[{}] - Processing transaction failed register event", command.id());

        entityManager
                .update(TransactionJournal.class)
                .set("failureCode", command.code())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleDescribed(DescribeTransactionCommand command) {
        log.info("[{}] - Processing transaction describe event", command.id());

        entityManager
                .update(TransactionJournal.class)
                .set("description", command.description())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleSplitTransaction(SplitTransactionCommand command) {
        log.info("[{}] - Processing transaction split event", command.id());

        var transaction = entityManager.getDetached(
                TransactionJournal.class, Collections.Map("id", command.id()));
        var splits = Collections.List(command.split());

        var survivors = splits.map(SplitTransactionCommand.Part::id).reject(Objects::isNull);

        // Mark all old parts as deleted
        var deletedIds = Collections.List(transaction.getTransactions())
                .reject(t -> survivors.contains(t.getId()))
                .map(TransactionJpa::getId);

        entityManager
                .update(TransactionJpa.class)
                .set("deleted", new Date())
                .fieldEqOneOf("id", deletedIds.stream().toArray())
                .execute();

        // Add new parts
        splits.filter(part -> part.id() == null)
                .map(part -> new TransactionJpa(
                        entityManager.getById(AccountJpa.class, part.accountId()),
                        transaction,
                        BigDecimal.valueOf(part.amount()),
                        part.description()))
                .forEach(entityPart -> {
                    transaction.getTransactions().add(entityPart);
                    entityManager.persist(entityPart);
                });

        // Update existing parts
        splits.filter(part -> Objects.nonNull(part.id())).forEach(part -> entityManager
                .update(TransactionJpa.class)
                .set("amount", part.amount())
                .fieldEq("id", part.id())
                .execute());
    }

    @EventListener
    public void handleTag(TagTransactionCommand command) {
        log.info("[{}] - Processing transaction tagging event", command.id());

        var transaction = entityManager.getById(TransactionJournal.class, command.id());
        transaction.getTags().clear();

        command.tags().map(this::tag).filter(Objects::nonNull).forEach(tag -> transaction
                .getTags()
                .add(tag));

        entityManager.persist(transaction);
    }

    @EventListener
    public void handleDelete(DeleteTransactionCommand command) {
        log.info("[{}] - Processing transaction delete event", command.id());

        entityManager
                .update(TransactionJournal.class)
                .set("deleted", new Date())
                .fieldEq("id", command.id())
                .execute();

        entityManager
                .update(TransactionJpa.class)
                .set("deleted", new Date())
                .fieldEq("journal.id", command.id())
                .execute();
    }

    private TagJpa tag(String name) {
        return entityManager
                .from(TagJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .getOrThrow(() -> new IllegalArgumentException("tag not found"));
    }
}
