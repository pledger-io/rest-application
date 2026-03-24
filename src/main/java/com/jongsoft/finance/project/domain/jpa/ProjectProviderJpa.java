package com.jongsoft.finance.project.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.adapter.api.ProjectProvider;
import com.jongsoft.finance.project.domain.jpa.entity.ProjectJpa;
import com.jongsoft.finance.project.domain.jpa.mapper.ProjectMapper;
import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class ProjectProviderJpa implements ProjectProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final ProjectMapper projectMapper;

    public ProjectProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            ProjectMapper projectMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.projectMapper = projectMapper;
    }

    @Override
    public Optional<Project> lookup(long id) {
        log.trace("Project lookup by id {}.", id);

        return entityManager
                .from(ProjectJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(projectMapper::toDomain);
    }

    @Override
    public Optional<Project> lookup(String name) {
        log.trace("Project lookup by name {}.", name);

        return entityManager
                .from(ProjectJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false)
                .singleResult()
                .map(projectMapper::toDomain);
    }

    @Override
    public Sequence<Project> lookup(String name, Long clientId, Boolean billableOnly) {
        log.trace(
                "Project listing, name {}, clientId {}, billableOnly {}.",
                name,
                clientId,
                billableOnly);

        var query = entityManager
                .from(ProjectJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("archived", false);

        if (name != null && !name.isBlank()) {
            query = query.fieldLike("name", name);
        }
        if (clientId != null) {
            query = query.fieldEq("client.id", clientId);
        }
        if (Boolean.TRUE.equals(billableOnly)) {
            query = query.fieldEq("billable", true);
        }

        return query.stream()
                .map(projectMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }
}
