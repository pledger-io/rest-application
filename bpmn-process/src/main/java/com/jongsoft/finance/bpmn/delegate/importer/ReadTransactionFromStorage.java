package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.importer.api.TransactionDTO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;

/**
 * Reads a transaction from storage.
 * <p>
 *     This delegate reads a transaction from storage using the {@code storageToken} provided in the process variables.
 *     The transaction is then stored in the process variables as {@code transaction}.
 *     The transaction is stored as a {@link TransactionDTO} object.
 * </p>
 */
@Singleton
public class ReadTransactionFromStorage implements JavaDelegate, JavaBean {

    private final StorageService storageService;
    private final ProcessMapper processMapper;

    @Inject
    public ReadTransactionFromStorage(StorageService storageService, ProcessMapper processMapper) {
        this.storageService = storageService;
        this.processMapper = processMapper;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var storageToken = (String) delegateExecution.getVariableLocal("storageToken");

        var transaction = storageService.read(storageToken)
                .map(byteArray -> new String(byteArray, StandardCharsets.UTF_8))
                .map(json -> processMapper.readSafe(json, TransactionDTO.class))
                .getOrThrow(() -> new RuntimeException("Failed to read transaction from storage"));

        delegateExecution.setVariableLocal("transaction", transaction);
    }

}
