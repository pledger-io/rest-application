package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * This delegate accesses the {@link Account} synonym list in the system. The synonym list
 * contains a relation between potential account names encountered during import and previously selected actual
 * {@link Account}'s.
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>name, the name to use to look in the synonym list</li>
 * </ul>
 * <p>
 * The delegate will produce the following output:
 * <ul>
 *     <li>id, the account id</li>
 * </ul>
 */
@Slf4j
@Singleton
public class AccountSynonymLookupDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;

    AccountSynonymLookupDelegate(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing account lookup using synonym '{}'", execution.getCurrentActivityName(),
                execution.getVariable("name"));

        var synonym = execution.<StringValue>getVariableLocalTyped("name").getValue();
        var accountId = accountProvider.synonymOf(synonym)
                .map(Account::getId)
                .blockOptional(Duration.of(500, ChronoUnit.MILLIS))
                .orElse(null);

        execution.setVariableLocal("id", accountId);
    }

}
