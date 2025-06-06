package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.providers.ImportProvider;
import jakarta.inject.Singleton;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Slf4j
@Singleton
public class ImportFinishedDelegate implements JavaDelegate, JavaBean {

  private final StorageService storageService;
  private final ImportProvider importProvider;

  ImportFinishedDelegate(StorageService storageService, ImportProvider importProvider) {
    this.storageService = storageService;
    this.importProvider = importProvider;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var slug = execution.<StringValue>getVariableLocalTyped("importSlug").getValue();
    @SuppressWarnings("unchecked")
    var importTokens = (List<String>) execution.getVariable("storageToken");

    log.debug("{}: Finalizing importer job {}", execution.getCurrentActivityName(), slug);

    importProvider.lookup(slug).ifPresent(entity -> entity.finish(new Date()));

    importTokens.forEach(storageService::remove);
  }
}
