package com.jongsoft.finance.core.domain.model;

import com.jongsoft.finance.core.domain.commands.EnableModuleCommand;

import io.micronaut.core.annotation.Introspected;

import java.util.Set;
import java.util.UUID;

@Introspected
public class PledgerModule {
    private final UUID id;
    private final String code;
    private Set<PledgerModule> requiredModules;

    private boolean enabled;

    PledgerModule(UUID id, String code, Set<PledgerModule> requiredModules, boolean enabled) {
        this.id = id;
        this.code = code;
        this.requiredModules = requiredModules;
        this.enabled = enabled;
    }

    public void enable() {
        enabled = true;
        EnableModuleCommand.moduleEnabled(id);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Set<PledgerModule> getRequiredModules() {
        return Set.copyOf(requiredModules);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
