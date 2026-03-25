package com.jongsoft.finance.invoice;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class InvoiceModule implements Module {
    private static final UUID ID = UUID.fromString("b2c3d4e5-6f7g-8h9i-0j1k-l2m3n4o5p6q7");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.invoice";
    }

    @Override
    public String getCode() {
        return "INVOICE";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING", "PROJECT");
    }
}
