package com.jongsoft.finance.importer;

import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import com.jongsoft.finance.importer.api.TransactionConsumer;

public interface ImporterProvider<T extends ImporterConfiguration> {

    void readTransactions(TransactionConsumer consumer, ImporterConfiguration updatedConfiguration, BatchImport importJob);

    T loadConfiguration(BatchImportConfig batchImportConfig);

    <X extends ImporterConfiguration> boolean supports(X configuration);

    default String getImporterType() {
        return this.getClass().getSimpleName();
    }

}
