package com.jongsoft.finance.contract;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class ContractModule implements Module {
    private static final UUID ID = UUID.fromString("e0d28a96-560a-44a2-a7f8-a49b22f04e0a");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.contract";
    }

    @Override
    public String getCode() {
        return "CONTRACT";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING");
    }
}
