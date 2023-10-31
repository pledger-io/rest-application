package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.tuple.Triplet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.ArrayList;
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
        execution.setVariableLocal("locatable", new ArrayList<Triplet<String, String, String>>());
    }

    @Override
    protected void lineRead(DelegateExecution execution, ParsedTransaction parsedTransaction) {
        var triple = API.Tuple(
                parsedTransaction.getOpposingName(),
                parsedTransaction.getOpposingIBAN(),
                parsedTransaction.getDescription());

        @SuppressWarnings("unchecked")
        var locatable = (ArrayList<Triplet<String, String, String>>) execution.getVariableLocal("locatable");
        locatable.add(triple);
    }

    @Override
    protected void afterProcess(DelegateExecution execution) {
        execution.setVariable("extractionResult", new HashSet<>());
    }
}
