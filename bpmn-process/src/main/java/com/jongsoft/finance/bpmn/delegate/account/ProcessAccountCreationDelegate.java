package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.AccountJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

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
public class ProcessAccountCreationDelegate implements JavaDelegate, JavaBean {

    private final CurrentUserProvider userProvider;
    private final AccountProvider accountProvider;
    private final StorageService storageService;
    private final ProcessMapper mapper;

    ProcessAccountCreationDelegate(
            CurrentUserProvider userProvider,
            AccountProvider accountProvider,
            StorageService storageService,
            ProcessMapper mapper) {
        this.userProvider = userProvider;
        this.accountProvider = accountProvider;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) {
        var accountJson = mapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("account").getValue(),
                AccountJson.class);

        log.debug("{}: Processing account creation from json '{}'",
                execution.getCurrentActivityName(),
                accountJson.getName());

        accountProvider.lookup(accountJson.getName())
                .ifNotPresent(() -> {
                    userProvider.currentUser().createAccount(
                            accountJson.getName(),
                            accountJson.getCurrency(),
                            accountJson.getType());

                    accountProvider.lookup(accountJson.getName())
                            .ifPresent(account -> {
                                account.changeAccount(
                                        handleEmptyAsNull(accountJson.getIban()),
                                        handleEmptyAsNull(accountJson.getBic()),
                                        handleEmptyAsNull(accountJson.getNumber()));
                                account.rename(
                                        accountJson.getName(),
                                        accountJson.getDescription(),
                                        accountJson.getCurrency(),
                                        accountJson.getType());

                                if (accountJson.getPeriodicity() != null) {
                                    account.interest(accountJson.getInterest(), accountJson.getPeriodicity());
                                }

                                if (accountJson.getIcon() != null) {
                                    account.registerIcon(storageService.store(Hex.decode(accountJson.getIcon())));
                                }
                            });
                });
    }

    private String handleEmptyAsNull(String value) {
        if (value != null && value.trim().length() > 0) {
            return value;
        }

        return null;
    }
}
