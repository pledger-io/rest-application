package com.jongsoft.finance.project.domain.jpa.mapper;

import com.jongsoft.finance.project.domain.jpa.entity.ProjectJpa;
import com.jongsoft.finance.project.domain.model.ClientIdentifier;
import com.jongsoft.finance.project.domain.model.Project;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public abstract class ProjectMapper {

    private final ClientMapper clientMapper;

    protected ProjectMapper(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    @Mapper.Mapping(to = "client", from = "#{this.mapClient(entity)}")
    public abstract Project toDomain(ProjectJpa entity);

    public ClientIdentifier mapClient(ProjectJpa entity) {
        return clientMapper.toIdentifier(entity.getClient());
    }
}
