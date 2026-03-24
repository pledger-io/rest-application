package com.jongsoft.finance.project.domain.jpa.mapper;

import com.jongsoft.finance.project.domain.jpa.entity.TimeEntryJpa;
import com.jongsoft.finance.project.domain.model.TimeEntry;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public abstract class TimeEntryMapper {

    private final ProjectMapper projectMapper;

    protected TimeEntryMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Mapper.Mapping(to = "project", from = "#{this.mapProject(entity)}")
    public abstract TimeEntry toDomain(TimeEntryJpa entity);

    public com.jongsoft.finance.project.domain.model.Project mapProject(TimeEntryJpa entity) {
        return projectMapper.toDomain(entity.getProject());
    }
}
