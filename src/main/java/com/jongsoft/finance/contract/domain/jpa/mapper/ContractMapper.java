package com.jongsoft.finance.contract.domain.jpa.mapper;

import com.jongsoft.finance.contract.domain.jpa.entity.ContractJpa;
import com.jongsoft.finance.contract.domain.model.Contract;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface ContractMapper {

    @Mapper.Mapping(to = "uploaded", from = "#{entity.fileToken != null}")
    @Mapper.Mapping(to = "notifyBeforeEnd", from = "#{entity.warningActive}")
    @Mapper.Mapping(to = "terminated", from = "#{entity.archived}")
    Contract mapToDomain(ContractJpa entity);
}
