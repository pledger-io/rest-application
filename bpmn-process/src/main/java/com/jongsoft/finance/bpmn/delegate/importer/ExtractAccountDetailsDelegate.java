package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ExtractedAccountLookup;
import com.jongsoft.finance.serialized.ImportConfigJson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.HashSet;

@Singleton
public class ExtractAccountDetailsDelegate extends CSVReaderDelegate {

    @Inject
    public ExtractAccountDetailsDelegate(
            ImportProvider importProvider,
            StorageService storageService,
            ProcessMapper mapper) {
        super(importProvider, storageService, mapper);
    }

    @Override
    protected void beforeProcess(DelegateExecution execution, ImportConfigJson configJson) {
        execution.setVariableLocal("locatable", new HashSet<>());
    }

    @Override
    protected void lineRead(DelegateExecution execution, ParsedTransaction parsedTransaction) {
        var extraction = new ExtractedAccountLookup(
                parsedTransaction.getOpposingName(),
                parsedTransaction.getOpposingIBAN(),
                parsedTransaction.getDescription());

        @SuppressWarnings("unchecked")
        var locatable = (HashSet<ExtractedAccountLookup>) execution.getVariableLocal("locatable");
        locatable.add(extraction);
    }

    @Override
    protected void afterProcess(DelegateExecution execution) {
        execution.setVariable("extractionResult", new HashSet<>());
    }
}
