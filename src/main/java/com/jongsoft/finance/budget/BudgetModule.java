package com.jongsoft.finance.budget;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class BudgetModule implements Module {
    private static final UUID ID = UUID.fromString("6d86192c-6570-46ec-bfd1-b00c50ce7ab9");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.budget";
    }

    @Override
    public String getCode() {
        return "BUDGET";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING");
    }
}
