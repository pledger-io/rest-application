package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.ModuleProvider;
import com.jongsoft.finance.core.domain.jpa.entity.ModuleJpa;
import com.jongsoft.finance.core.domain.jpa.mapper.PledgerModuleMapper;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.model.PledgerModule;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

@ReadOnly
@Singleton
class ModuleProviderJpa implements ModuleProvider {

    private final PledgerModuleMapper moduleMapper;
    private final ReactiveEntityManager entityManager;

    ModuleProviderJpa(PledgerModuleMapper moduleMapper, ReactiveEntityManager entityManager) {
        this.moduleMapper = moduleMapper;
        this.entityManager = entityManager;
    }

    @Override
    public boolean isModuleEnabled(String code) {
        return entityManager
                .from(ModuleJpa.class)
                .fieldEq("moduleCode", code)
                .singleResult()
                .map(ModuleJpa::isEnabled)
                .getOrThrow(() -> StatusException.internalError("Module not found " + code));
    }

    @Override
    public Optional<PledgerModule> getModule(String code) {
        return entityManager
                .from(ModuleJpa.class)
                .fieldEq("moduleCode", code)
                .singleResult()
                .map(moduleMapper::map);
    }
}
