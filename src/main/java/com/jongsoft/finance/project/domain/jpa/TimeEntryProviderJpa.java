package com.jongsoft.finance.project.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.adapter.api.TimeEntryProvider;
import com.jongsoft.finance.project.domain.jpa.entity.TimeEntryJpa;
import com.jongsoft.finance.project.domain.jpa.mapper.TimeEntryMapper;
import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@ReadOnly
@Singleton
class TimeEntryProviderJpa implements TimeEntryProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final TimeEntryMapper timeEntryMapper;

    public TimeEntryProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            TimeEntryMapper timeEntryMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.timeEntryMapper = timeEntryMapper;
    }

    @Override
    public Optional<TimeEntry> lookup(long id) {
        log.trace("TimeEntry lookup by id {}.", id);

        return entityManager
                .from(TimeEntryJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(timeEntryMapper::toDomain);
    }

    @Override
    public Sequence<TimeEntry> lookup(
            LocalDate startDate, LocalDate endDate, Long projectId, Boolean invoiced) {
        log.trace(
                "Time entry listing, {} - {}, projectId {}, invoiced {}.",
                startDate,
                endDate,
                projectId,
                invoiced);

        var query = entityManager
                .from(TimeEntryJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldBetween("date", startDate, endDate);

        if (projectId != null) {
            query = query.fieldEq("project.id", projectId);
        }
        if (invoiced != null) {
            query = query.fieldEq("invoiced", invoiced);
        }

        return query.orderBy("id", true).stream()
                .map(timeEntryMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }
}
