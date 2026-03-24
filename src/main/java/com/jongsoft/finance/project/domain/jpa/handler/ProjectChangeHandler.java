package com.jongsoft.finance.project.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.domain.commands.ArchiveProjectCommand;
import com.jongsoft.finance.project.domain.commands.CreateProjectCommand;
import com.jongsoft.finance.project.domain.commands.UpdateProjectCommand;
import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;
import com.jongsoft.finance.project.domain.jpa.entity.ProjectJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class ProjectChangeHandler {
    private final Logger log = LoggerFactory.getLogger(ProjectChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    ProjectChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateProject(CreateProjectCommand command) {
        log.info("[{}] - Processing project create event", command.name());

        var client = entityManager
                .from(ClientJpa.class)
                .fieldEq("id", command.clientId())
                .singleResult()
                .get();

        var toCreate = ProjectJpa.of(
                command.name(),
                command.description(),
                client,
                command.startDate(),
                command.endDate(),
                command.billable(),
                entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateProject(UpdateProjectCommand command) {
        log.info("[{}] - Processing project update event", command.id());

        entityManager
                .update(ProjectJpa.class)
                .set("name", command.name())
                .set("description", command.description())
                .set("startDate", command.startDate())
                .set("endDate", command.endDate())
                .set("billable", command.billable())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleArchiveProject(ArchiveProjectCommand command) {
        log.info("[{}] - Processing project archive event", command.id());

        entityManager
                .update(ProjectJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
