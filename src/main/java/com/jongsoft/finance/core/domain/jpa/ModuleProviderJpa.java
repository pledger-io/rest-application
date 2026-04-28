package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.ModuleProvider;
import com.jongsoft.finance.core.domain.jpa.entity.ModuleJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

@ReadOnly
@Singleton
class ModuleProviderJpa implements ModuleProvider {

    private final ReactiveEntityManager entityManager;

    ModuleProviderJpa(ReactiveEntityManager entityManager) {
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
}
