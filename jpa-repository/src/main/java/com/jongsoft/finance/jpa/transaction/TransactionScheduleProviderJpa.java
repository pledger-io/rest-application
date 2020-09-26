package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.TransactionScheduleProvider;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.transaction.entity.ScheduledTransactionJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.SynchronousTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.time.LocalDate;

@Singleton
@Named("transactionScheduleProvider")
public class TransactionScheduleProviderJpa extends DataProviderJpa<ScheduledTransaction, ScheduledTransactionJpa>
        implements TransactionScheduleProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public TransactionScheduleProviderJpa(
            AuthenticationFacade authenticationFacade,
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, ScheduledTransactionJpa.class, transactionManager);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<ScheduledTransaction> lookup() {
        var hql = """
                select s from ScheduledTransactionJpa s
                where s.user.username = :username
                    and (s.end > :currentDate or s.end is null)""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("currentDate", LocalDate.now());

        return this.<ScheduledTransactionJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    protected ScheduledTransaction convert(ScheduledTransactionJpa source) {
        if (source == null) {
            return null;
        }

        return ScheduledTransaction.builder()
                .id(source.getId())
                .name(source.getName())
                .description(source.getDescription())
                .schedule(new ScheduleValue(source.getPeriodicity(), source.getInterval()))
                .start(source.getStart())
                .end(source.getEnd())
                .source(Account.builder()
                        .id(source.getSource().getId())
                        .name(source.getSource().getName())
                        .type(source.getSource().getType().getLabel())
                        .build())
                .destination(Account.builder()
                        .id(source.getDestination().getId())
                        .name(source.getDestination().getName())
                        .type(source.getDestination().getType().getLabel())
                        .build())
                .amount(source.getAmount())
                .build();
    }

}
