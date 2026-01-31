package com.jongsoft.finance.spending;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class SpendingModule implements Module {
    private static final UUID ID = UUID.fromString("e854748b-78fa-4476-b433-1a02f73971bf");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.spending";
    }

    @Override
    public String getCode() {
        return "SPENDING";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING", "CLASSIFICATION");
    }
}
