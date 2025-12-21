package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.messaging.notifications.TransactionCreated;
import com.jongsoft.finance.providers.DataProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateTransactionHandler
        implements CommandHandler<CreateTransactionCommand>, TransactionCreationHandler {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;
    private final List<DataProvider<? extends Classifier>> metadataProviders;

    @Inject
    public CreateTransactionHandler(
            ReactiveEntityManager entityManager,
            AuthenticationFacade authenticationFacade,
            List<DataProvider<? extends Classifier>> metadataProviders) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.metadataProviders = metadataProviders;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateTransactionCommand command) {
        handleCreatedEvent(command);
    }

    @Override
    public long handleCreatedEvent(CreateTransactionCommand command) {
        log.info(
                "[{}] - Processing transaction create event",
                command.transaction().getDescription());

        var jpaEntity = TransactionJournal.builder()
                .date(command.transaction().getDate())
                .bookDate(command.transaction().getBookDate())
                .interestDate(command.transaction().getInterestDate())
                .description(command.transaction().getDescription())
                .currency(entityManager
                        .from(CurrencyJpa.class)
                        .fieldEq("code", command.transaction().getCurrency())
                        .singleResult()
                        .get())
                .user(entityManager.currentUser())
                .type(TransactionType.valueOf(
                        command.transaction().computeType().name()))
                .failureCode(command.transaction().getFailureCode())
                .transactions(new HashSet<>())
                .tags(Control.Option(command.transaction().getTags())
                        .map(Sequence::distinct)
                        .map(set -> set.map(this::tag).toJava())
                        .getOrSupply(() -> null))
                .build();

        entityManager.persist(jpaEntity);

        for (Transaction.Part transfer : command.transaction().getTransactions()) {
            // todo change to native BigDecimal later on
            var transferJpa = TransactionJpa.builder()
                    .amount(BigDecimal.valueOf(transfer.getAmount()))
                    .account(entityManager.getById(
                            AccountJpa.class, transfer.getAccount().getId()))
                    .journal(jpaEntity)
                    .build();

            jpaEntity.getTransactions().add(transferJpa);
            entityManager.persist(transferJpa);
        }

        if (command.transaction().getMetadata() != null) {
            for (var relation : command.transaction().getMetadata().entrySet()) {
                var relatedEntity = metadataProviders.stream()
                        .filter(provider -> Objects.equals(provider.typeOf(), relation.getKey()))
                        .findFirst()
                        .map(provider -> provider.lookup(relation.getValue().getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get);
                relatedEntity.ifPresent(entity -> {
                    var metaDataEntity =
                            new TransactionMetaJpa(jpaEntity, relation.getKey(), entity.getId());
                    jpaEntity.getMetadata().add(metaDataEntity);
                    entityManager.persist(metaDataEntity);
                });
            }
        }

        TransactionCreated.transactionCreated(jpaEntity.getId());
        return jpaEntity.getId();
    }

    private TagJpa tag(String name) {
        return entityManager
                .from(TagJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .getOrSupply(() -> null);
    }
}
