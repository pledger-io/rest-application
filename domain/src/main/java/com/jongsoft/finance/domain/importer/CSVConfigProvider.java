package com.jongsoft.finance.domain.importer;

import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface CSVConfigProvider {

    Optional<BatchImportConfig> lookup(String name);
    Sequence<BatchImportConfig> lookup();

}
