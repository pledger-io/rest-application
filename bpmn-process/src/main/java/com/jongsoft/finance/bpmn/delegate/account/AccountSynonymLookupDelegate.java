package com.jongsoft.finance.bpmn.delegate.account;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;

import lombok.extern.slf4j.Slf4j;

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

    @Inject
    public AccountSynonymLookupDelegate(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing account lookup using synonym '{}'", execution.getCurrentActivityName(),
                execution.getVariable("name"));

        var synonym = execution.<StringValue>getVariableLocalTyped("name").getValue();
        accountProvider.synonymOf(synonym)
                .ifPresent(account -> execution.setVariableLocal("id", account.getId()))
                .elseRun(() -> execution.setVariableLocal("id", null));
    }

}
