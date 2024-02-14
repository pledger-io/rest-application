package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Singleton
public class LoadImporterConfiguration implements JavaDelegate {

    private final ImportProvider importProvider;
    private final StorageService storageService;
    private final ProcessMapper mapper;

    @Inject
    public LoadImporterConfiguration(
            ImportProvider importProvider,
            StorageService storageService,
            ProcessMapper mapper) {
        this.importProvider = importProvider;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var batchImportSlug = (String) delegateExecution.getVariableLocal("batchImportSlug");

        log.debug("{}: Loading default import configuration for {}.",
                delegateExecution.getCurrentActivityName(),
                batchImportSlug);

        var importJob = importProvider.lookup(batchImportSlug)
                .getOrThrow(() -> new IllegalStateException("Cannot find batch import with slug " + batchImportSlug));

        var importConfig = readConfiguration(importJob.getConfig().getFileCode());

        delegateExecution.setVariableLocal("importConfig", importConfig);
    }

    private ImportConfigJson readConfiguration(String fileCode) {
        return storageService.read(fileCode)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .map(json -> mapper.readSafe(json, ImportConfigJson.class))
                .getOrThrow(() -> new IllegalStateException("Cannot read import configuration from file " + fileCode));
    }
}
