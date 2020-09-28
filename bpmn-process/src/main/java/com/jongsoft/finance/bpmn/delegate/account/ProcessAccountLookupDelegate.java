package com.jongsoft.finance.bpmn.delegate.account;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.lang.API;
import com.jongsoft.lang.control.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * This delegate allows for locating {@link Account} instances within the system using the following methods:
 *
 * <ol>
 *     <li><strong>id</strong>, the unique identifier</li>
 *     <li><strong>iban</strong>, the unique IBAN of the account</li>
 *     <li><strong>name</strong>, the unique name of the account</li>
 * </ol>
 *
 * The output of this delegate will be:
 * <ul>
 *     <li>{@code account}, the {@link Account} found</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessAccountLookupDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;
    private final FilterFactory accountFilterFactory;

    @Inject
    public ProcessAccountLookupDelegate(
            AccountProvider accountProvider,
            FilterFactory accountFilterFactory) {
        this.accountProvider = accountProvider;
        this.accountFilterFactory = accountFilterFactory;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.debug("{}: Processing account lookup '{}' - {} [{}]",
                execution.getCurrentActivityName(),
                execution.getVariable("name"),
                execution.getVariableLocal("iban"),
                execution.getVariableLocal("id"));

        Optional<Account> matchedAccount = API.Option();
        if (execution.hasVariableLocal("id") && execution.getVariableLocal("id") != null) {
            matchedAccount = accountProvider.lookup((Long) execution.getVariableLocal("id"));
        }

        if (!matchedAccount.isPresent() && execution.hasVariableLocal("iban")) {
             final String iban = (String) execution.getVariableLocal("iban");
            if (iban != null && iban.trim().length() > 0) {
                matchedAccount = accountProvider.lookup(accountFilterFactory.account().iban(iban, true))
                        .content()
                        .first(x -> true);
            }
        }

        if (!matchedAccount.isPresent() && execution.hasVariableLocal("name")) {
            final String accountName = (String) execution.getVariableLocal("name");
            matchedAccount = accountProvider.lookup(accountName);
        }

        log.trace("{}: Processing account located {}", execution.getCurrentActivityName(), matchedAccount);
        execution.setVariableLocal("id", matchedAccount.map(Account::getId).getOrSupply(() -> null));
    }

}