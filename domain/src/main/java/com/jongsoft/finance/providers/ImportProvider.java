package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.lang.control.Optional;

public interface ImportProvider extends DataProvider<BatchImport> {

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

    default boolean supports(Class<?> supportingClass) {
        return BatchImport.class.equals(supportingClass);
    }

    default String typeOf() {
        return "IMPORT";
    }
}
