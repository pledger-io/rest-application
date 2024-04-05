package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ImportConfigurationProvider {

    Optional<BatchImportConfig> lookup(String name);
    Sequence<BatchImportConfig> lookup();

}
