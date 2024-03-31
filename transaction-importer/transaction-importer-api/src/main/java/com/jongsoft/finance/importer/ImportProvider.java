package com.jongsoft.finance.importer;

import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import com.jongsoft.finance.importer.api.TransactionConsumer;

public interface ImportProvider<T extends ImporterConfiguration> {

    void readTransactions(TransactionConsumer consumer, T updatedConfiguration, BatchImport importJob);

    T loadConfiguration(BatchImportConfig batchImportConfig);

}
