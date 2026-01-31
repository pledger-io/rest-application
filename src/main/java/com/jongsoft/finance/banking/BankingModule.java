package com.jongsoft.finance.banking;

import com.jongsoft.finance.configuration.Module;

import java.util.UUID;

class BankingModule implements Module {
    private static final UUID ID = UUID.fromString("10c67221-1e9b-463d-b7e3-7a70f5172847");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.banking";
    }

    @Override
    public String getCode() {
        return "BANKING";
    }
}
