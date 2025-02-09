package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.time.LocalDate;

@ReadOnly
@Singleton
@RequiresJpa
@Named("transactionScheduleProvider")
public class TransactionScheduleProviderJpa implements TransactionScheduleProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public TransactionScheduleProviderJpa(AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ScheduledTransaction> lookup(long id) {
        return entityManager.from(ScheduledTransactionJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public ResultPage<ScheduledTransaction> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof ScheduleFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate)
                    .paged()
                    .map(this::convert);
        }

        throw new IllegalStateException("Cannot use non JPA filter on TransactionScheduleProviderJpa");
    }

    @Override
    public Sequence<ScheduledTransaction> lookup() {
        return entityManager.from(ScheduledTransactionJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldGtOrEqNullable("end", LocalDate.now())
                .stream()
                .map(this::convert)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
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
