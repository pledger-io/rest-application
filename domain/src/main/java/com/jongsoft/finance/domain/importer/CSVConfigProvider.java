package com.jongsoft.finance.domain.importer;

import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;

public interface CSVConfigProvider {

    Optional<BatchImportConfig> lookup(String name);
    Flowable<BatchImportConfig> lookup();

}
