package com.jongsoft.finance.bpmn.delegate.account;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.AccountJson;

import lombok.extern.slf4j.Slf4j;

/**
 * This delegate reads a JSON serialized as a variable and processes it into an
 * {@link Account} that will be created.
 *
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>account, the serialized {@link AccountJson} used to create the account</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessAccountCreationDelegate implements JavaDelegate {

    private final CurrentUserProvider userProvider;
    private final AccountProvider accountProvider;

    @Inject
    public ProcessAccountCreationDelegate(
            CurrentUserProvider userProvider,
            AccountProvider accountProvider) {
        this.userProvider = userProvider;
        this.accountProvider = accountProvider;
    }

    @Override
    public void execute(DelegateExecution execution) {
        var accountJson = ProcessMapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("account").getValue(),
                AccountJson.class);

        log.debug("{}: Processing account creation from json '{}'",
                execution.getCurrentActivityName(),
                accountJson.getName());

        if (!accountProvider.lookup(accountJson.getName()).isPresent()) {
            userProvider.currentUser().createAccount(
                    accountJson.getName(),
                    accountJson.getCurrency(),
                    accountJson.getType());

            var account = accountProvider.lookup(accountJson.getName()).get();
            account.changeAccount(
                    handleEmptyAsNull(accountJson.getIban()),
                    handleEmptyAsNull(accountJson.getBic()),
                    handleEmptyAsNull(accountJson.getNumber()));
            account.rename(
                    accountJson.getName(),
                    accountJson.getDescription(),
                    accountJson.getCurrency(),
                    accountJson.getType());
        }
    }

    private String handleEmptyAsNull(String value) {
        if (value != null && value.trim().length() > 0) {
            return value;
        }

        return null;
    }
}
