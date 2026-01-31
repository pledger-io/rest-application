package com.jongsoft.finance.exporter.adapter.api;

import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ImportConfigurationProvider {

    Optional<BatchImportConfig> lookup(String name);

    Sequence<BatchImportConfig> lookup();
}
