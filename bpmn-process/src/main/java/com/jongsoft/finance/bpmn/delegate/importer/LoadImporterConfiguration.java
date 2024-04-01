package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.importer.ImporterProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportJobSettings;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class LoadImporterConfiguration implements JavaDelegate {

    private final ImportProvider importProvider;
    private final ImporterProvider<?> importerProvider;

    public LoadImporterConfiguration(ImportProvider importProvider, ImporterProvider<?> importerProvider) {
        this.importProvider = importProvider;
        this.importerProvider = importerProvider;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var batchImportSlug = (String) delegateExecution.getVariableLocal("batchImportSlug");

        log.debug("{}: Loading default import configuration for {}.",
                delegateExecution.getCurrentActivityName(),
                batchImportSlug);

        var importJob = importProvider.lookup(batchImportSlug)
                .getOrThrow(() -> new IllegalStateException("Cannot find batch import with slug " + batchImportSlug));

        var configuration = importerProvider.loadConfiguration(importJob.getConfig());

        delegateExecution.setVariableLocal(
                "importConfig",
                new ImportJobSettings(configuration, false, false, null));
    }
}
