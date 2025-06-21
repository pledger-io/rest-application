package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.importer.ImporterProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportJobSettings;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * Loads the importer configuration for the given batch import. The importer configuration is loaded
 * from the importer provider that matches the import type and is stored in the {@code importConfig}
 * variable.
 */
@Slf4j
@Singleton
public class LoadImporterConfiguration implements JavaDelegate, JavaBean {

  private final ImportProvider importProvider;
  private final List<ImporterProvider<?>> importerProvider;

  public LoadImporterConfiguration(
      ImportProvider importProvider, List<ImporterProvider<?>> importerProvider) {
    this.importProvider = importProvider;
    this.importerProvider = importerProvider;
  }

  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {
    var batchImportSlug = (String) delegateExecution.getVariableLocal("batchImportSlug");

    log.debug(
        "{}: Loading default import configuration for {}.",
        delegateExecution.getCurrentActivityName(),
        batchImportSlug);

    var importJob = importProvider
        .lookup(batchImportSlug)
        .getOrThrow(() ->
            new IllegalStateException("Cannot find batch import with slug " + batchImportSlug));

    importerProvider.stream()
        .filter(importer ->
            importer.getImporterType().equalsIgnoreCase(importJob.getConfig().getType()))
        .findFirst()
        .ifPresentOrElse(
            importer -> delegateExecution.setVariableLocal(
                "importConfig",
                new ImportJobSettings(
                    importer.loadConfiguration(importJob.getConfig()), false, false, null)),
            () -> {
              throw new IllegalStateException(
                  "Cannot find importer for type " + importJob.getConfig().getType());
            });
  }
}
