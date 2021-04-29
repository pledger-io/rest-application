package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.SplitTransactionCommand;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Singleton
@Transactional
public class SplitTransactionHandler implements CommandHandler<SplitTransactionCommand> {

    private final ReactiveEntityManager entityManager;

    public SplitTransactionHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(SplitTransactionCommand command) {
        log.info("[{}] - Processing transaction split event", command.id());

        var transaction = entityManager.getDetached(
                TransactionJournal.class,
                Collections.Map("id", command.id()));

        var survivors = command.split()
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

        entityManager.update()
                .hql(deleteHql)
                .set("ids", deletedIds.toJava())
                .set("now", new Date())
                .execute();

        // Add new parts
        command.split()
                .filter(part -> part.getId() == null)
                .map(part -> TransactionJpa.builder()
                        // todo change to native BigDecimal later on
                        .amount(BigDecimal.valueOf(part.getAmount()))
                        .description(part.getDescription())
                        .account(entityManager.get(AccountJpa.class, Collections.Map("id", part.getAccount().getId())))
                        .journal(transaction)
                        .build())
                .forEach(entityPart -> {
                    transaction.getTransactions().add(entityPart);
                    entityManager.persist(entityPart);
                });

        // Update existing parts
        command.split()
                .filter(part -> Objects.nonNull(part.getId()))
                .forEach(part -> entityManager.update()
                        .hql("""
                                update TransactionJpa
                                set amount = :amount
                                where id = :id""")
                        .set("id", part.getId())
                        // todo change later on to BigDecimal native
                        .set("amount", BigDecimal.valueOf(part.getAmount()))
                        .execute());
    }

}
