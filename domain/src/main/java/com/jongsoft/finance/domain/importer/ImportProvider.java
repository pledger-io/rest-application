package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.domain.core.ResultPage;
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

    Optional<BatchImport> lookup(String slug);
    ResultPage<BatchImport> lookup(FilterCommand filter);

}
