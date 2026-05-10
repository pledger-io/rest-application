package com.jongsoft.finance.core.domain.jpa.handler;

import com.jongsoft.finance.core.domain.commands.EnableModuleCommand;
import com.jongsoft.finance.core.domain.jpa.entity.ModuleJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;

@Singleton
@Transactional
class ModuleHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(ModuleHandler.class);
    private final ReactiveEntityManager entityManager;

    public ModuleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    void handleEnableCommand(EnableModuleCommand command) {
        log.info("Enabling module {}", command.id());

        entityManager
                .update(ModuleJpa.class)
                .set("enabled", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
