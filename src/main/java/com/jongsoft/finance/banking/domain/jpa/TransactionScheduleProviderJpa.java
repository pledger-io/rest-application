package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.banking.domain.jpa.filter.TransactionScheduleFilterCommand;
import com.jongsoft.finance.banking.domain.jpa.handler.TransactionScheduleMapper;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.LocalDate;

@ReadOnly
@Singleton
public class TransactionScheduleProviderJpa implements TransactionScheduleProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    private final TransactionScheduleMapper mapper;

    @Inject
    public TransactionScheduleProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            TransactionScheduleMapper transactionScheduleMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.mapper = transactionScheduleMapper;
    }

    @Override
    public Optional<TransactionSchedule> lookup(long id) {
        return entityManager
                .from(TransactionScheduleJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public ResultPage<TransactionSchedule> lookup(FilterCommand filterCommand) {
        if (filterCommand instanceof TransactionScheduleFilterCommand delegate) {
            delegate.user(authenticationFacade.authenticated());

            return entityManager.from(delegate).paged().map(mapper::toModel);
        }

        throw new IllegalStateException(
                "Cannot use non JPA filter on TransactionScheduleProviderJpa");
    }

    @Override
    public Sequence<TransactionSchedule> lookup() {
        return entityManager
                .from(TransactionScheduleJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldGtOrEqNullable("end", LocalDate.now())
                .stream()
                .map(mapper::toModel)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }
}
