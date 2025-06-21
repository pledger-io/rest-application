package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.importer.api.TransactionDTO;
import com.jongsoft.finance.serialized.AccountJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * This is a delegate that prepares the account generation. It reads the transaction token from the
 * execution and reads the transaction from the storage. It then writes the account JSON to the
 * execution in the property {@code accountJson}.
 */
@Slf4j
@Singleton
public class PrepareAccountGenerationDelegate implements JavaDelegate, JavaBean {
  private final ProcessMapper mapper;

  PrepareAccountGenerationDelegate(ProcessMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var transaction = (TransactionDTO) execution.getVariableLocal("transaction");

    log.debug(
        "{}: Extracting the account to be created from the transaction {}.",
        execution.getCurrentActivityName(),
        transaction.opposingName());

    var accountJson = AccountJson.builder()
        .name(transaction.opposingName())
        .iban(transaction.opposingIBAN())
        .type(transaction.type() == TransactionType.CREDIT ? "creditor" : "debtor")
        .currency("EUR") // todo this needs to be fixed later on
        .build();

    execution.setVariableLocal("accountJson", mapper.writeSafe(accountJson));
  }
}
