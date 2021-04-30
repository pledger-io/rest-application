package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDate;

@Singleton
@Named("transactionScheduleProvider")
public class TransactionScheduleProviderJpa implements TransactionScheduleProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public TransactionScheduleProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ScheduledTransaction> lookup(long id) {
        return entityManager.<ScheduledTransactionJpa>blocking()
                .hql("from ScheduledTransactionJpa where id = :id and user.username = :username")
                .set("id", id)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<ScheduledTransaction> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof ScheduleFilterCommand delegate) {
            var offset = delegate.page() * delegate.pageSize();
            delegate.user(authenticationFacade.authenticated());

            return entityManager.<ScheduledTransactionJpa>blocking()
                    .hql(delegate.generateHql())
                    .setAll(delegate.getParameters())
                    .limit(delegate.pageSize())
                    .offset(offset)
                    .sort(delegate.sort())
                    .page()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionScheduleProviderJpa");
    }

    @Override
    public Sequence<ScheduledTransaction> lookup() {
        var hql = """
                select s from ScheduledTransactionJpa s
                where s.user.username = :username
                    and (s.end > :currentDate or s.end is null)""";

        return entityManager.<ScheduledTransactionJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("currentDate", LocalDate.now())
                .sequence()
                .map(this::convert);
    }

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
                .contract(build(source.getContract()))
                .amount(source.getAmount())
                .build();
    }

    private Contract build(ContractJpa source) {
        if (source == null) {
            return null;
        }

        return Contract.builder()
                .id(source.getId())
                .startDate(source.getStartDate())
                .endDate(source.getEndDate())
                .build();
    }

}
