package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/**
 * This delegate is responsible for locating the mapping between account names and {@link Account#getId()} during the
 * import. This delegate works using a {@code accountMapping} JSON map string to lookup the account id belonging to the
 * account name.
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>accountMapping, a JSON serialized map containing the account name as property and account id as value</li>
 *     <li>name, the name of the account to lookup</li>
 * </ul>
 * The delegate will produce the following output:
 * <ul>
 *     <li>accountId, the account id matching the name</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LookupAccountMappingDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;
    private final StorageService storageService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String accountName = (String) execution.getVariableLocal("name");

        var mappings = retrieveMappings(execution);
        final Object mappedId = mappings.get(accountName);

        if (mappedId != null && !mappedId.toString().isBlank()) {
            final Long mappedToAccountId = Long.valueOf(mappedId.toString());
            log.debug(
                    "{}: Processing account mapping lookup '{}' matches '{}'",
                    execution.getCurrentActivityName(),
                    accountName,
                    mappedToAccountId);

            processAccountMapped(accountName, mappedToAccountId);
            execution.setVariableLocal("accountId", mappedToAccountId);
        } else {
            execution.setVariableLocal("accountId", null);
        }
    }

    private void processAccountMapped(String accountName, Long mappedToAccountId) {
        var account = accountProvider.lookup(mappedToAccountId).get();
        if (!account.getName().equalsIgnoreCase(accountName)) {
            log.trace("Linking account {} to synonym {}", account.getName(), accountName);
            account.registerSynonym(accountName);
        }
    }

    private Map retrieveMappings(DelegateExecution execution) throws IOException {
        var fileCode = execution.getVariable("accountMapping").toString();

        var rawValue = storageService.read(fileCode)
                .blockingGet();
        return ProcessMapper.INSTANCE.readValue(rawValue, Map.class);
    }

}
