package com.jongsoft.finance.core.domain.jpa.mapper;

import com.jongsoft.finance.core.domain.jpa.entity.ModuleJpa;
import com.jongsoft.finance.core.domain.model.PledgerModule;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface PledgerModuleMapper {

    @Mapper.Mapping(to = "code", from = "moduleCode")
    PledgerModule map(ModuleJpa moduleJpa);
}
