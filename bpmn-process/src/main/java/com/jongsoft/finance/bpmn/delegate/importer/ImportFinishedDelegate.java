package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImportFinishedDelegate implements JavaDelegate {

    private final StorageService storageService;
    private final ImportProvider importProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var slug = execution.<StringValue>getVariableLocalTyped("slug").getValue();
        var importTokens = (List<String>) execution.getVariable("transactionTokens");

        log.debug("{}: Finalizing importer job {}",
                execution.getCurrentActivityName(),
                slug);

        importProvider.lookup(slug)
                .subscribe(entity -> entity.finish(new Date()));

        importTokens.forEach(storageService::remove);
    }

}
