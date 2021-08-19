package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.lang.control.Optional;
import reactor.core.publisher.Flux;

public interface CSVConfigProvider {

    Optional<BatchImportConfig> lookup(String name);
    Flux<BatchImportConfig> lookup();

}
