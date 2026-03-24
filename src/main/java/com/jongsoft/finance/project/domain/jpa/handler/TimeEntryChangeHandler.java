package com.jongsoft.finance.project.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.domain.commands.CreateTimeEntryCommand;
import com.jongsoft.finance.project.domain.commands.DeleteTimeEntryCommand;
import com.jongsoft.finance.project.domain.commands.UpdateTimeEntryCommand;
import com.jongsoft.finance.project.domain.jpa.entity.ProjectJpa;
import com.jongsoft.finance.project.domain.jpa.entity.TimeEntryJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class TimeEntryChangeHandler {
    private final Logger log = LoggerFactory.getLogger(TimeEntryChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    TimeEntryChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateTimeEntry(CreateTimeEntryCommand command) {
        log.info(
                "[{}] - Processing time entry create event for project {}",
                command.date(),
                command.projectId());

        var project = entityManager
                .from(ProjectJpa.class)
                .fieldEq("id", command.projectId())
                .singleResult()
                .get();

        var toCreate = TimeEntryJpa.of(
                project,
                command.date(),
                command.hours(),
                command.description(),
                entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateTimeEntry(UpdateTimeEntryCommand command) {
        log.info("[{}] - Processing time entry update event", command.id());

        entityManager
                .update(TimeEntryJpa.class)
                .set("date", command.date())
                .set("hours", command.hours())
                .set("description", command.description())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleDeleteTimeEntry(DeleteTimeEntryCommand command) {
        log.info("[{}] - Processing time entry delete event", command.id());

        entityManager
                .from(TimeEntryJpa.class)
                .fieldEq("id", command.id())
                .singleResult()
                .ifPresent(entity -> entityManager.getEntityManager().remove(entity));
    }
}
