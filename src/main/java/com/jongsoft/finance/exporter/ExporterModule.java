package com.jongsoft.finance.exporter;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class ExporterModule implements Module {
    private static final UUID ID = UUID.fromString("d5f79448-0c97-475e-8e62-25738d1a9c1e");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.exporter";
    }

    @Override
    public String getCode() {
        return "EXPORTER";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING", "CONTRACT", "BUDGET", "CLASSIFICATION");
    }
}
