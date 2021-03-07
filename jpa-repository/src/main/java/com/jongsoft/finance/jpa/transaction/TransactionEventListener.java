package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionCreationHandler;
import com.jongsoft.finance.domain.transaction.events.*;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.transaction.entity.TagJpa;
import com.jongsoft.finance.jpa.transaction.entity.TransactionJournal;
import com.jongsoft.finance.jpa.transaction.entity.TransactionJpa;
import com.jongsoft.finance.jpa.user.entity.CategoryJpa;
import com.jongsoft.finance.jpa.user.entity.ExpenseJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

@Singleton
@Transactional
public class TransactionEventListener extends RepositoryJpa implements TransactionCreationHandler {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public TransactionEventListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    @BusinessEventListener
    public long handleCreatedEvent(TransactionCreatedEvent event) {
        logger.trace("[{}] - Processing transaction create event", event.getTransaction().getDescription());
        var jpaEntity = TransactionJournal.builder()
                .date(event.getTransaction().getDate())
                .bookDate(event.getTransaction().getBookDate())
                .interestDate(event.getTransaction().getInterestDate())
                .description(event.getTransaction().getDescription())
                .currency(currency(event.getTransaction().getCurrency()))
                .user(activeUser())
                .type(TransactionType.valueOf(event.getTransaction().computeType().name()))
                .failureCode(event.getTransaction().getFailureCode())
                .transactions(new HashSet<>())
                .category(
                        Control.Option(event.getTransaction().getCategory())
                                .map(this::category)
                                .getOrSupply(() -> null))
                .budget(
                        Control.Option(event.getTransaction().getBudget())
                                .map(this::expense)
                                .getOrSupply(() -> null))
                .contract(
                        Control.Option(event.getTransaction().getContract())
                                .map(this::contract)
                                .getOrSupply(() -> null))
                .tags(
                        Control.Option(event.getTransaction().getTags())
                                .map(Sequence::distinct)
                                .map(set -> set.map(this::tag).toJava())
                                .getOrSupply(() -> null))
                .batchImport(Control.Option(event.getTransaction().getImportSlug())
                        .map(this::job)
                        .getOrSupply(() -> null))
                .build();

        entityManager.persist(jpaEntity);

        for (Transaction.Part transfer : event.getTransaction().getTransactions()) {
            var transferJpa = TransactionJpa.builder()
                    .amount(transfer.getAmount())
                    .account(entityManager.find(AccountJpa.class, transfer.getAccount().getId()))
                    .journal(jpaEntity)
                    .build();

            jpaEntity.getTransactions().add(transferJpa);
            entityManager.persist(transferJpa);
        }

        return jpaEntity.getId();
    }

    @BusinessEventListener
    public void handleFailureRegistrationEvent(TransactionFailureEvent event) {
        logger.trace("[{}] - Processing transaction failed register event", event.getTransactionId());

        var hql = """
                update TransactionJournal 
                set failureCode = :failureCode
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getTransactionId());
        query.setParameter("failureCode", event.getFailureCode());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleDescribeEvent(TransactionDescribeEvent event) {
        logger.trace("[{}] - Processing transaction describe event", event.getTransactionId());
        var hqlTransaction = """
                update TransactionJournal 
                set description = :description
                where id = :id""";

        var queryTransaction = entityManager.createQuery(hqlTransaction);
        queryTransaction.setParameter("id", event.getTransactionId());
        queryTransaction.setParameter("description", event.getDescription());
        queryTransaction.executeUpdate();
    }

    @BusinessEventListener
    public void handleAmountChangedEvent(TransactionAmountChangedEvent event) {
        logger.trace("[{}] - Processing transaction amount change event", event.getTransactionId());

        var hql = """
                update TransactionJpa 
                set amount = case when amount >= 0 
                                then :amount 
                                else :negAmount end
                where journal.id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getTransactionId());
        query.setParameter("amount", event.getAmount());
        query.setParameter("negAmount", -event.getAmount());
        query.executeUpdate();

        var hqlTransaction = """
                update TransactionJournal 
                set currency = :currency
                where id = :id""";

        var queryTransaction = entityManager.createQuery(hqlTransaction);
        queryTransaction.setParameter("id", event.getTransactionId());
        queryTransaction.setParameter("currency", currency(event.getCurrency()));
        queryTransaction.executeUpdate();
    }

    @BusinessEventListener
    public void handleBookedEvent(TransactionBookedEvent event) {
        logger.trace("[{}] - Processing transaction book event", event.getTransactionId());

        var hql = """
                update TransactionJournal 
                set date = :date,
                    bookDate = :bookDate,
                    interestDate = :interestDate
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getTransactionId());
        query.setParameter("bookDate", event.getBookDate());
        query.setParameter("date", event.getDate());
        query.setParameter("interestDate", event.getInterestDate());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleRelationUpdate(TransactionRelationEvent event) {
        logger.trace("[{}] - Processing transaction relation change {}", event.getId(), event.getType());
        var hql = new StringBuilder("update TransactionJournal set");

        switch (event.getType()) {
            case CATEGORY -> hql.append(" category.id = ").append(category(event.getRelation()).getId());
            case CONTRACT -> hql.append(" contract.id = ").append(contract(event.getRelation()).getId());
            case EXPENSE -> hql.append(" budget.id = ").append(expense(event.getRelation()).getId());
        }

        hql.append(" where id = :id");
        var query = entityManager.createQuery(hql.toString());
        query.setParameter("id", event.getId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleAccountChangedEvent(TransactionAccountChangedEvent event) {
        logger.trace("[{}] - Processing transaction account change", event.getTransactionPartId());

        var hql = """
                update TransactionJpa 
                set account.id = :accountId
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getTransactionPartId());
        query.setParameter("accountId", event.getAccount().getId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleTagEvent(TransactionTaggingEvent event) {
        logger.trace("[{}] - Processing transaction tagging event", event.getId());

        var transaction = entityManager.find(TransactionJournal.class, event.getId());
        transaction.getTags().clear();

        event.getTags()
                .map(this::tag)
                .filter(Objects::nonNull)
                .forEach(tag -> transaction.getTags().add(tag));

        entityManager.persist(transaction);
    }

    @BusinessEventListener
    public void handleSplitEvent(TransactionSplitEvent event) {
        logger.trace("[{}] - Processing transaction split event", event.getTransactionId());

        var transaction = entityManager.find(TransactionJournal.class, event.getTransactionId());
        entityManager.detach(transaction);

        var survivors = event.getTransactionParts()
                .map(Transaction.Part::getId)
                .reject(Objects::isNull);

        // Mark all old parts as deleted
        var deletedIds = Collections.List(transaction.getTransactions())
                .reject(t -> survivors.contains(t.getId()))
                .map(TransactionJpa::getId);
        var deleteHql = """
                update TransactionJpa 
                set deleted = :now
                where id in (:ids)""";
        var deleteQuery = entityManager.createQuery(deleteHql);
        deleteQuery.setParameter("ids", deletedIds.toJava());
        deleteQuery.setParameter("now", new Date());
        deleteQuery.executeUpdate();

        // Add new parts
        event.getTransactionParts()
                .filter(part -> part.getId() == null)
                .map(part -> TransactionJpa.builder()
                        .amount(part.getAmount())
                        .description(part.getDescription())
                        .account(entityManager.find(AccountJpa.class, part.getAccount().getId()))
                        .journal(transaction)
                        .build())
                .forEach(entityPart -> {
                    transaction.getTransactions().add(entityPart);
                    entityManager.persist(entityPart);
                });

        // Update existing parts
        event.getTransactionParts()
                .filter(part -> Objects.nonNull(part.getId()))
                .forEach(part -> {
                    var updateHql = """
                            update TransactionJpa
                            set amount = :amount
                            where id = :id""";
                    var query = entityManager.createQuery(updateHql);
                    query.setParameter("id", part.getId());
                    query.setParameter("amount", part.getAmount());
                    query.executeUpdate();
                });
    }

    @BusinessEventListener
    public void handleDeletedEvent(TransactionDeletedEvent event) {
        logger.trace("[{}] - Processing transaction delete event", event.getTransactionId());

        var query = entityManager.createQuery("update TransactionJournal set deleted = :now where id = :id");
        query.setParameter("id", event.getTransactionId());
        query.setParameter("now", new Date());
        query.executeUpdate();

        var updateHql = """
                update TransactionJpa
                set deleted = :now
                where journal_id = :id""";
        query = entityManager.createQuery(updateHql);
        query.setParameter("id", event.getTransactionId());
        query.setParameter("now", new Date());
        query.executeUpdate();
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select u from UserAccountJpa u where u.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }

    private CategoryJpa category(String label) {
        var hql = """
                select c from CategoryJpa c 
                where c.label = :label and c.user.username = :username""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("label", label);
        return singleValue(query);
    }

    private CurrencyJpa currency(String currency) {
        var hql = """
                select c from CurrencyJpa c 
                where c.code = :code""";
        var query = entityManager.createQuery(hql);
        query.setParameter("code", currency);
        return singleValue(query);
    }

    private ExpenseJpa expense(String name) {
        var hql = """
                select e from ExpenseJpa e
                where e.name = :name and e.user.username = :username""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);
        return singleValue(query);
    }

    private ContractJpa contract(String name) {
        var hql = """
                select e from ContractJpa e
                where e.name = :name and e.user.username = :username""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);
        return singleValue(query);
    }

    private ImportJpa job(String slug) {
        var hql = """
                select e from ImportJpa e
                where e.slug = :slug and e.user.username = :username""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("slug", slug);
        return singleValue(query);
    }

    private TagJpa tag(String name) {
        var hql = """
                select t from TagJpa t
                where t.name = :name and t.user.username = :username""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);
        return singleValue(query);
    }

}
