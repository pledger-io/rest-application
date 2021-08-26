package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import reactor.core.publisher.Mono;

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

    Mono<BatchImport> lookup(String slug);
    ResultPage<BatchImport> lookup(FilterCommand filter);

}
