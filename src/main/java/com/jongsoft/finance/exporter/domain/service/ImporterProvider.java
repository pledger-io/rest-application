package com.jongsoft.finance.exporter.domain.service;

import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.finance.exporter.domain.model.ImporterConfiguration;

public interface ImporterProvider<T extends ImporterConfiguration> {

    void readTransactions(
            TransactionConsumer consumer,
            ImporterConfiguration updatedConfiguration,
            BatchImport importJob);

    T loadConfiguration(BatchImportConfig batchImportConfig);

    default String getImporterType() {
        return this.getClass().getSimpleName();
    }
}
