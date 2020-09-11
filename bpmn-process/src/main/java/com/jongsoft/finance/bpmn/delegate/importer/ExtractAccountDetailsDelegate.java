package com.jongsoft.finance.bpmn.delegate.importer;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.tuple.Pair;
import com.jongsoft.lang.collection.tuple.Triplet;

@Singleton
public class ExtractAccountDetailsDelegate extends CSVReaderDelegate {

    @Inject
    public ExtractAccountDetailsDelegate(
            ImportProvider importProvider,
            StorageService storageService) {
        super(importProvider, storageService);
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
        execution.setVariable("resultSet", new ArrayList<Pair<String, Number>>());
    }
}
