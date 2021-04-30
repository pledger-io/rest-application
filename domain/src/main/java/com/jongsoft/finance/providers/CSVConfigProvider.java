package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;

public interface CSVConfigProvider {

    Optional<BatchImportConfig> lookup(String name);
    Flowable<BatchImportConfig> lookup();

}
