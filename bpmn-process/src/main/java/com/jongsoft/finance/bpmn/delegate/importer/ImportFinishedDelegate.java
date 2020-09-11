package com.jongsoft.finance.bpmn.delegate.importer;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.domain.importer.ImportProvider;

@Singleton
public class ImportFinishedDelegate implements JavaDelegate {

    private final ImportProvider importProvider;

    @Inject
    public ImportFinishedDelegate(ImportProvider importProvider) {
        this.importProvider = importProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var slug = execution.<StringValue>getVariableLocalTyped("slug").getValue();

        importProvider.lookup(slug)
                .ifPresent(entity -> entity.finish(new Date()));
    }

}
