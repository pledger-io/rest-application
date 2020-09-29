package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.domain.core.ResultPage;
import io.reactivex.Maybe;

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

    Maybe<BatchImport> lookup(String slug);
    ResultPage<BatchImport> lookup(FilterCommand filter);

}
