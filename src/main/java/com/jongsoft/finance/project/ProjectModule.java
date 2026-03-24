package com.jongsoft.finance.project;

import com.jongsoft.finance.configuration.Module;

import java.util.List;
import java.util.UUID;

class ProjectModule implements Module {
    private static final UUID ID = UUID.fromString("a1b2c3d4-5e6f-7g8h-9i0j-k1l2m3n4o5p6");

    @Override
    public UUID getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "module.project";
    }

    @Override
    public String getCode() {
        return "PROJECT";
    }

    @Override
    public List<String> requiresModules() {
        return List.of("BANKING");
    }
}
