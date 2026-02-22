package com.jongsoft.finance.classification;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class ClassificationModule implements Module {
    private static final UUID ID = UUID.fromString("43b95efc-8e0d-4d3c-9466-3b62079839c3");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.classification";
    }

    @Override
    public String getCode() {
        return "CLASSIFICATION";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING");
    }
}
