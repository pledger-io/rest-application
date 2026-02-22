package com.jongsoft.finance.configuration;

import java.util.List;
import java.util.UUID;

public interface Module {

    UUID getId();

    String getName();

    String getCode();

    default List<String> requiresModules() {
        return List.of();
    }
}
