package com.jongsoft.finance.exporter.adapter.api;

import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ImportProvider {

    interface FilterCommand {
        default int page() {
            return 0;
        }

        default int pageSize() {
            return Integer.MAX_VALUE;
        }

        static FilterCommand unpaged() {
            return new FilterCommand() {};
        }
    }

    Sequence<BatchImport> lookup();

    Optional<BatchImport> lookup(long id);

    Optional<BatchImport> lookup(String slug);

    ResultPage<BatchImport> lookup(FilterCommand filter);
}
