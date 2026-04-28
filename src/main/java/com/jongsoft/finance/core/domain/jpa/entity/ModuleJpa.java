package com.jongsoft.finance.core.domain.jpa.entity;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_module")
public class ModuleJpa {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    private String moduleCode;
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "app_module_requirement")
    private Set<ModuleJpa> requiredModules;

    public UUID getId() {
        return id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<ModuleJpa> getRequiredModules() {
        return requiredModules;
    }
}
