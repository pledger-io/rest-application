package com.jongsoft.finance.project.domain.jpa.mapper;

import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;
import com.jongsoft.finance.project.domain.model.Client;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
@Mapper
public interface ClientMapper {

    Client toDomain(ClientJpa entity);
}
